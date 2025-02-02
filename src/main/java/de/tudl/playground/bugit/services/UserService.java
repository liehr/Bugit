package de.tudl.playground.bugit.services;

import de.tudl.playground.bugit.dtos.LoginRequest;
import de.tudl.playground.bugit.dtos.RegisterRequest;
import de.tudl.playground.bugit.models.User;
import de.tudl.playground.bugit.repositories.UserRepository;
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
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RequestStatusStore statusStore;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       RequestStatusStore statusStore,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.statusStore = statusStore;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user.
     * On successful registration, sets the status to "SUCCESS" in the status store.
     * On error, logs the error and sets the status to "FAILED".
     *
     * @param request the registration request containing user details and a requestId.
     */
    public void register(RegisterRequest request) {
        User user = buildUserFromRegisterRequest(request);
        try {
            userRepository.save(user);
            statusStore.setStatus(request.getRequestId(), "SUCCESS");
        } catch (Exception e) {
            log.error("Error during registration for request {}: {}", request.getRequestId(), e.getMessage());
            statusStore.setStatus(request.getRequestId(), "FAILED");
            // Depending on your requirements, you might want to rethrow an application-specific exception here.
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
        User user = userRepository.findUserByEmail(request.email())
                .orElseThrow(() -> {
                    log.error("User with Email {} not found.", request.email());
                    return new UsernameNotFoundException("User with email " + request.email() + " not found");
                });

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.password())
        );

        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(user.getUsername());
        } else {
            log.error("Authentication failed for user: {}", user.getUsername());
            throw new IllegalStateException("Authentication failed for user: " + user.getUsername());
        }
    }

    /**
     * Converts a RegisterRequest DTO to a User entity.
     *
     * @param request the registration request.
     * @return a new User entity.
     */
    private User buildUserFromRegisterRequest(RegisterRequest request) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return user;
    }
}
