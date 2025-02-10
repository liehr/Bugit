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

/**
 * Service class for managing user operations such as registration, authentication,
 * and profile updates.
 * <p>
 * This service handles user registration by creating a new user and an associated default
 * budget, verifying user credentials to generate JWT tokens, updating user details, and
 * determining premium status for the currently authenticated user.
 * </p>
 */
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

    /**
     * Constructs a new {@code UserService} with the required dependencies.
     *
     * @param userRepository          repository for user data.
     * @param authenticationManager   manager for authenticating user credentials.
     * @param jwtService              service for generating JWT tokens.
     * @param statusStore             store to keep track of request statuses.
     * @param passwordEncoder         encoder for encrypting user passwords.
     * @param authenticationService   service for retrieving the currently authenticated user.
     * @param budgetRepository        repository for budget data.
     * @param encryptionService       service for encrypting/decrypting sensitive data.
     */
    public UserService(UserRepository userRepository,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       RequestStatusStore statusStore,
                       BCryptPasswordEncoder passwordEncoder,
                       AuthenticationService authenticationService,
                       BudgetRepository budgetRepository,
                       EncryptionService encryptionService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.statusStore = statusStore;
        this.passwordEncoder = passwordEncoder;
        this.authenticationService = authenticationService;
        this.budgetRepository = budgetRepository;
        this.encryptionService = encryptionService;
    }

    /**
     * Registers a new user.
     * <p>
     * Builds a new {@link User} entity from the {@link RegisterRequest}, saves it to the repository,
     * creates a default {@link Budget} for the user (with an initial amount of 5, encrypted), and
     * updates the request status accordingly in the {@link RequestStatusStore}.
     * </p>
     *
     * @param request the registration request containing user details.
     */
    public void register(RegisterRequest request) {
        try {
            User user = buildUserFromRegisterRequest(request);
            userRepository.save(user);

            Budget budget = new Budget();
            budget.setId(UUID.randomUUID());
            budget.setAmount(encryptionService.encrypt("5"));
            budget.setUser(user);
            
            budgetRepository.save(budget);

            statusStore.setStatus(request.getRequestId(), "SUCCESS");
        } catch (Exception e) {
            log.error("Error during registration for request {}: {}", request.getRequestId(), e.getMessage(), e);
            statusStore.setStatus(request.getRequestId(), "FAILED");
        }
    }

    /**
     * Verifies a login request and generates a JWT token upon successful authentication.
     * <p>
     * The method retrieves the user by email, attempts authentication using the provided password,
     * and if authenticated, generates a JWT token that encodes the user's username and premium status.
     * </p>
     *
     * @param request the login request containing email and password.
     * @return a JWT token if authentication is successful.
     * @throws UsernameNotFoundException if no user is found for the provided email.
     * @throws IllegalStateException     if authentication fails.
     */
    public String verify(LoginRequest request) {
        return userRepository.findUserByEmail(request.email())
                .map(user -> {
                    Authentication authentication = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(user.getUsername(), request.password()));
                    if (authentication.isAuthenticated()) {
                        return jwtService.generateToken(user.getUsername(), user.isPremium());
                    }
                    log.error("Authentication failed for user: {}", user.getUsername());
                    throw new IllegalStateException("Authentication failed for user: " + user.getUsername());
                })
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + request.email() + " not found"));
    }

    /**
     * Builds a new {@link User} entity from a {@link RegisterRequest}.
     *
     * @param request the registration request.
     * @return a new {@link User} with a generated UUID and an encoded password.
     */
    private User buildUserFromRegisterRequest(RegisterRequest request) {
        return new User(
                UUID.randomUUID(),
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                false);
    }

    /**
     * Updates the details of the currently authenticated user.
     * <p>
     * The user's email and username are updated based on the provided {@link UpdateUserRequest}
     * and the updated user is saved to the repository.
     * </p>
     *
     * @param request the request containing new user details.
     * @return a {@link UserResponse} representing the updated user.
     * @throws UnauthorizedException if no user is authenticated.
     */
    @SneakyThrows
    public UserResponse updateUser(UpdateUserRequest request) {
        return Optional.of(getAuthenticatedUser())
                .map(user -> {
                    user.setEmail(request.email());
                    user.setUsername(request.username());
                    userRepository.save(user);
                    return new UserResponse(
                            user.getId().toString(),
                            user.getUsername(),
                            user.getEmail(),
                            user.isPremium());
                })
                .orElseThrow(() -> new UnauthorizedException("User not authorized!"));
    }

    /**
     * Checks if the currently authenticated user has a premium status.
     *
     * @return {@code true} if the user is premium; {@code false} otherwise.
     * @throws UnauthorizedException if no user is authenticated.
     */
    public boolean isUserPremium() {
        return getAuthenticatedUser().isPremium();
    }

    /**
     * Retrieves the currently authenticated user.
     *
     * @return the authenticated {@link User}.
     * @throws UnauthorizedException if no user is authenticated.
     */
    @SneakyThrows
    private User getAuthenticatedUser() {
        return Optional.ofNullable(authenticationService.getCurrentUser())
                .orElseThrow(() -> new UnauthorizedException("User not authorized!"));
    }
}
