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

import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RequestStatusStore statusStore;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;

    public UserService(UserRepository userRepository, BudgetRepository budgetRepository,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       RequestStatusStore statusStore,
                       BCryptPasswordEncoder passwordEncoder, AuthenticationService authenticationService) {
        this.userRepository = userRepository;
        this.budgetRepository = budgetRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.statusStore = statusStore;
        this.passwordEncoder = passwordEncoder;
        this.authenticationService = authenticationService;
    }

    /**
     * Registers a new user.
     * On successful registration, sets the status to "SUCCESS" in the status store.
     * On error, logs the error and sets the status to "FAILED".
     *
     * @param request the registration request containing user details and a requestId.
     */
    public void register(RegisterRequest request) {
        final User user = buildUserFromRegisterRequest(request);
        try {
            userRepository.save(user);
            statusStore.setStatus(request.getRequestId(), "SUCCESS");
        } catch (Exception e) {
            log.error("Error during registration for request {}: {}", request.getRequestId(), e.getMessage(), e);
            statusStore.setStatus(request.getRequestId(), "FAILED");
        }
    }

    /**
     * Authenticates the user and returns a JWT token on success.
     *
     * @param request the login request containing email and password.
     * @return the generated JWT token if authentication is successful.
     * @throws UsernameNotFoundException if the user with the given email is not found.
     * @throws IllegalStateException     if authentication fails.
     */
    public String verify(LoginRequest request) {
        final User user = userRepository.findUserByEmail(request.email())
                .orElseThrow(() -> {
                    log.error("User with Email {} not found.", request.email());
                    return new UsernameNotFoundException("User with email " + request.email() + " not found");
                });

        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.password())
        );

        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(user.getUsername(), user.isPremium());
        }

        log.error("Authentication failed for user: {}", user.getUsername());
        throw new IllegalStateException("Authentication failed for user: " + user.getUsername());
    }

    /**
     * Converts a RegisterRequest DTO to a User entity.
     *
     * @param request the registration request.
     * @return a new User entity.
     */
    private User buildUserFromRegisterRequest(RegisterRequest request) {

        return User.builder()
                .id(UUID.randomUUID())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .premium(false)
                .build();
    }

    @SneakyThrows
    public UserResponse updateUser(UpdateUserRequest request) {
        User user = authenticationService.getCurrentUser();

        if(user == null)
            throw new UnauthorizedException("User not authorized");

        user.setEmail(request.email());
        user.setUsername(request.username());

        return new UserResponse(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.isPremium()
        );
    }

    @SneakyThrows
    public boolean isUserPremium()
    {
        User user = authenticationService.getCurrentUser();

        if (user == null)
            throw new UnauthorizedException("User is not authorized");

        return user.isPremium();
    }
}

