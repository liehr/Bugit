package de.tudl.playground.bugit.services;

import de.tudl.playground.bugit.dtos.requests.user.LoginRequest;
import de.tudl.playground.bugit.dtos.requests.user.RegisterRequest;
import de.tudl.playground.bugit.dtos.requests.user.UpdateUserRequest;
import de.tudl.playground.bugit.dtos.responses.UserResponse;
import de.tudl.playground.bugit.exception.UnauthorizedException;
import de.tudl.playground.bugit.models.Budget;
import de.tudl.playground.bugit.models.User;
import de.tudl.playground.bugit.repositories.BudgetRepository;
import de.tudl.playground.bugit.repositories.UserRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RequestStatusStore statusStore;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;
    private final BudgetRepository budgetRepository;
    private final EncryptionService encryptionService;

    public UserService(UserRepository userRepository, AuthenticationManager authenticationManager,
                       JwtService jwtService, RequestStatusStore statusStore,
                       BCryptPasswordEncoder passwordEncoder, AuthenticationService authenticationService, BudgetRepository budgetRepository, EncryptionService encryptionService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.statusStore = statusStore;
        this.passwordEncoder = passwordEncoder;
        this.authenticationService = authenticationService;
        this.budgetRepository = budgetRepository;
        this.encryptionService = encryptionService;
    }

    public void register(RegisterRequest request) {
        final User user = buildUserFromRegisterRequest(request);
        try {
            userRepository.save(user);
            Budget budget = new Budget();
            budget.setId(UUID.randomUUID());
            budget.setAmount(encryptionService.encrypt(String.valueOf(5)));
            budget.setUser(user);
            statusStore.setStatus(request.getRequestId(), "SUCCESS");
        } catch (Exception e) {
            log.error("Error during registration for request {}: {}", request.getRequestId(), e.getMessage(), e);
            statusStore.setStatus(request.getRequestId(), "FAILED");
        }
    }

    public String verify(LoginRequest request) {
        final User user = userRepository.findUserByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + request.email() + " not found"));

        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.password())
        );

        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(user.getUsername(), user.isPremium());
        }

        log.error("Authentication failed for user: {}", user.getUsername());
        throw new IllegalStateException("Authentication failed for user: " + user.getUsername());
    }

    private User buildUserFromRegisterRequest(RegisterRequest request) {
        return new User(UUID.randomUUID(), request.getUsername(), request.getEmail(),
                passwordEncoder.encode(request.getPassword()), false);
    }

    public UserResponse updateUser(UpdateUserRequest request) {
        User user = getAuthenticatedUser();
        user.setEmail(request.email());
        user.setUsername(request.username());
        userRepository.save(user);
        return new UserResponse(user.getId().toString(), user.getUsername(), user.getEmail(), user.isPremium());
    }

    public boolean isUserPremium() {
        return getAuthenticatedUser().isPremium();
    }

    @SneakyThrows
    private User getAuthenticatedUser() {
        return Optional.ofNullable(authenticationService.getCurrentUser())
                .orElseThrow(() -> new UnauthorizedException("User not authorized!"));
    }
}
