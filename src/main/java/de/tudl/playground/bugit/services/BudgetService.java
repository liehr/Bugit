package de.tudl.playground.bugit.services;

import de.tudl.playground.bugit.dtos.requests.budget.CreateBudgetRequest;
import de.tudl.playground.bugit.dtos.requests.budget.DeleteBudgetRequest;
import de.tudl.playground.bugit.dtos.requests.budget.UpdateBudgetRequest;
import de.tudl.playground.bugit.dtos.responses.BudgetResponse;
import de.tudl.playground.bugit.dtos.responses.BudgetResponseWithInvestments;
import de.tudl.playground.bugit.dtos.responses.InvestmentResponse;
import de.tudl.playground.bugit.exception.UnauthorizedException;
import de.tudl.playground.bugit.models.Budget;
import de.tudl.playground.bugit.models.Investment;
import de.tudl.playground.bugit.models.User;
import de.tudl.playground.bugit.repositories.BudgetRepository;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing budgets.
 * <p>
 * Provides operations to create, retrieve, update, and delete budgets. All operations
 * require an authenticated user. Budget amounts and related investment details are handled
 * using encryption/decryption via an {@code EncryptionService}.
 * </p>
 */
@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final AuthenticationService authenticationService;
    private final EncryptionService encryptionService;

    /**
     * Constructs a new BudgetService with the required dependencies.
     *
     * @param budgetRepository      repository for budget data.
     * @param authenticationService service for user authentication.
     * @param encryptionService     service for encryption and decryption.
     */
    public BudgetService(BudgetRepository budgetRepository,
                         AuthenticationService authenticationService,
                         EncryptionService encryptionService) {
        this.budgetRepository = budgetRepository;
        this.authenticationService = authenticationService;
        this.encryptionService = encryptionService;
    }

    /**
     * Creates a new budget for the authenticated user.
     *
     * @param request the request containing the amount for the new budget.
     * @return the created budget as a {@link BudgetResponse}.
     */
    public BudgetResponse createBudget(CreateBudgetRequest request) {
        return Optional.of(getAuthenticatedUser())
                .map(user -> {
                    Budget budget = new Budget();
                    budget.setId(UUID.randomUUID());
                    budget.setUser(user);
                    budget.setAmount(encrypt(String.valueOf(request.amount())));
                    return budgetRepository.save(budget);
                })
                .map(this::mapToResponse)
                .orElseThrow(); // This will never happen since getAuthenticatedUser() throws if user is null.
    }

    /**
     * Retrieves the budget for the authenticated user.
     * <p>
     * If no budget exists, a new one is created with a default amount of 5.
     * </p>
     *
     * @return the budget as a {@link BudgetResponse}.
     */
    public BudgetResponse getBudgetByUser() {
        User authenticatedUser = getAuthenticatedUser();
        return budgetRepository.findBudgetByUser(authenticatedUser)
                .map(this::mapToResponse)
                .orElseGet(() -> {
                    Budget newBudget = new Budget();
                    newBudget.setId(UUID.randomUUID());
                    newBudget.setUser(authenticatedUser);
                    newBudget.setAmount(encrypt(String.valueOf(5)));
                    return mapToResponse(budgetRepository.save(newBudget));
                });
    }

    /**
     * Retrieves the budget along with its associated investments for the authenticated user.
     *
     * @return the budget along with investments as a {@link BudgetResponseWithInvestments}.
     * @throws IllegalStateException if no budget is found for the user.
     */
    @SneakyThrows
    public BudgetResponseWithInvestments getBudgetWithInvestments() {
        return budgetRepository.findBudgetWithInvestmentsByUser(getAuthenticatedUser())
                .map(budget -> {
                    List<InvestmentResponse> investments = budget.getInvestments().stream()
                            .map(this::mapInvestmentToResponse)
                            .toList();
                    return new BudgetResponseWithInvestments(budget.getId(), decryptToInt(budget.getAmount()), investments);
                })
                .orElseThrow(() -> new IllegalStateException("No budget found for user"));
    }

    /**
     * Updates an existing budget for the authenticated user.
     *
     * @param request the request containing the budget ID and the new amount.
     * @return the updated budget as a {@link BudgetResponse}, or {@code null} if the budget is not found or the user is not authorized.
     */
    public BudgetResponse updateBudget(UpdateBudgetRequest request) {
        return budgetRepository.findById(UUID.fromString(request.budgetId()))
                .filter(budget -> budget.getUser().equals(getAuthenticatedUser()))
                .map(budget -> {
                    budget.setAmount(encrypt(String.valueOf(request.amount())));
                    return budgetRepository.save(budget);
                })
                .map(this::mapToResponse)
                .orElse(null);
    }

    /**
     * Deletes an existing budget for the authenticated user.
     *
     * @param request the request containing the budget ID to delete.
     * @return "Success" if deletion is successful, or {@code null} if the budget is not found or the user is not authorized.
     */
    public String deleteBudget(DeleteBudgetRequest request) {
        return budgetRepository.findById(UUID.fromString(request.budgetId()))
                .filter(budget -> budget.getUser().equals(getAuthenticatedUser()))
                .map(budget -> {
                    budgetRepository.delete(budget);
                    return "Success";
                })
                .orElse(null);
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

    /**
     * Maps a {@link Budget} entity to a {@link BudgetResponse}.
     *
     * @param budget the budget entity.
     * @return the corresponding {@link BudgetResponse}.
     */
    private BudgetResponse mapToResponse(Budget budget) {
        return new BudgetResponse(budget.getId(), decryptToInt(budget.getAmount()));
    }

    /**
     * Maps an {@link Investment} entity to an {@link InvestmentResponse}.
     *
     * @param investment the investment entity.
     * @return the corresponding {@link InvestmentResponse}.
     */
    private InvestmentResponse mapInvestmentToResponse(Investment investment) {
        return new InvestmentResponse(
                investment.getId(),
                decrypt(investment.getAsset()),
                decryptToInt(investment.getAmount()),
                decrypt(investment.getCategory()),
                decrypt(investment.getState()),
                decryptToInt(investment.getLiquidity()),
                investment.getBudget().getId()
        );
    }

    /**
     * Encrypts the given value using the {@link EncryptionService}.
     *
     * @param value the value to encrypt.
     * @return the encrypted value.
     */
    private String encrypt(String value) {
        return encryptionService.encrypt(value);
    }

    /**
     * Decrypts the given value using the {@link EncryptionService}.
     *
     * @param value the value to decrypt.
     * @return the decrypted value.
     */
    private String decrypt(String value) {
        return encryptionService.decrypt(value);
    }

    /**
     * Decrypts the given value and converts it to an integer.
     *
     * @param value the encrypted string representing an integer.
     * @return the decrypted integer.
     */
    private int decryptToInt(String value) {
        return Integer.parseInt(decrypt(value));
    }
}
