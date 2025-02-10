package de.tudl.playground.bugit.services;

import de.tudl.playground.bugit.dtos.requests.investment.CreateInvestmentRequest;
import de.tudl.playground.bugit.dtos.requests.investment.DeleteInvestmentRequest;
import de.tudl.playground.bugit.dtos.requests.investment.UpdateInvestmentRequest;
import de.tudl.playground.bugit.dtos.responses.InvestmentResponse;
import de.tudl.playground.bugit.exception.UnauthorizedException;
import de.tudl.playground.bugit.models.Budget;
import de.tudl.playground.bugit.models.Investment;
import de.tudl.playground.bugit.models.User;
import de.tudl.playground.bugit.repositories.BudgetRepository;
import de.tudl.playground.bugit.repositories.InvestmentRepository;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for managing investment operations.
 * <p>
 * This service provides methods to create, retrieve, update, and delete investment records
 * associated with the currently authenticated user. Investment details (such as asset, amount,
 * category, etc.) are stored in encrypted form using an {@link EncryptionService}. Each
 * investment is also linked to a user's budget.
 * </p>
 */
@Service
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final AuthenticationService authenticationService;
    private final EncryptionService encryptionService;
    private final BudgetRepository budgetRepository;

    /**
     * Constructs a new InvestmentService with the required dependencies.
     *
     * @param investmentRepository  repository for investment data.
     * @param authenticationService service for retrieving the currently authenticated user.
     * @param encryptionService     service for encryption and decryption.
     * @param budgetRepository      repository for budget data.
     */
    public InvestmentService(InvestmentRepository investmentRepository,
                             AuthenticationService authenticationService,
                             EncryptionService encryptionService,
                             BudgetRepository budgetRepository) {
        this.investmentRepository = investmentRepository;
        this.authenticationService = authenticationService;
        this.encryptionService = encryptionService;
        this.budgetRepository = budgetRepository;
    }

    /**
     * Creates a new investment record for the authenticated user.
     *
     * @param request the request containing investment details.
     * @return the created investment represented as an {@link InvestmentResponse}.
     * @throws IllegalStateException if no budget is found for the current user.
     * @throws UnauthorizedException if no user is authenticated.
     */
    @SneakyThrows
    public InvestmentResponse createInvestment(CreateInvestmentRequest request) {
        return Optional.of(getAuthenticatedUser())
                .flatMap(user ->
                        budgetRepository.findBudgetByUser(user)
                                .map(budget -> buildInvestment(request, user, budget))
                )
                .map(investmentRepository::save)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalStateException("No budget found for the current user."));
    }

    /**
     * Retrieves all investment records associated with the authenticated user.
     *
     * @return a list of {@link InvestmentResponse}; if no investments are found, an empty list is returned.
     * @throws UnauthorizedException if no user is authenticated.
     */
    @SneakyThrows
    public List<InvestmentResponse> getAllInvestmentsByUser() {
        User user = getAuthenticatedUser();
        return investmentRepository.findAllByUser(user)
                .map(investments -> investments.stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    /**
     * Updates an existing investment record for the authenticated user.
     *
     * @param request the request containing the investment ID and new details.
     * @return the updated investment represented as an {@link InvestmentResponse}.
     * @throws IllegalStateException if the investment is not found or unauthorized.
     * @throws UnauthorizedException if no user is authenticated.
     */
    @SneakyThrows
    public InvestmentResponse updateInvestment(UpdateInvestmentRequest request) {
        User user = getAuthenticatedUser();
        return Optional.of(getInvestmentByIdAndUser(request.investmentId(), user))
                .map(investment -> {
                    updateInvestmentFields(investment, request);
                    return investmentRepository.save(investment);
                })
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalStateException("Investment not found or unauthorized."));
    }

    /**
     * Deletes an existing investment record for the authenticated user.
     *
     * @param request the request containing the investment ID to delete.
     * @return "SUCCESS" if the deletion was successful.
     * @throws IllegalStateException if the investment is not found or unauthorized.
     * @throws UnauthorizedException if no user is authenticated.
     */
    @SneakyThrows
    public String deleteInvestment(DeleteInvestmentRequest request) {
        User user = getAuthenticatedUser();
        Investment investment = getInvestmentByIdAndUser(request.investmentId(), user);
        investmentRepository.delete(investment);
        return "SUCCESS";
    }

    // --- Helper Methods ---

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
     * Retrieves an investment by its ID for the given user.
     *
     * @param investmentId the investment ID as a string.
     * @param user         the authenticated user.
     * @return the {@link Investment} entity.
     * @throws IllegalStateException if the investment is not found or does not belong to the user.
     */
    private Investment getInvestmentByIdAndUser(String investmentId, User user) {
        return investmentRepository.findById(UUID.fromString(investmentId))
                .filter(inv -> inv.getUser().equals(user))
                .orElseThrow(() -> new IllegalStateException("Investment not found or unauthorized."));
    }

    /**
     * Builds an {@link Investment} entity from the creation request.
     *
     * @param request the investment creation request.
     * @param user    the authenticated user.
     * @param budget  the budget associated with the user.
     * @return a new {@link Investment} entity.
     */
    private Investment buildInvestment(CreateInvestmentRequest request, User user, Budget budget) {
        return Investment.builder()
                .id(UUID.randomUUID())
                .asset(encrypt(request.asset()))
                .amount(encrypt(String.valueOf(request.amount())))
                .category(encrypt(request.category()))
                .state(encrypt(request.state()))
                .liquidity(encrypt(String.valueOf(request.liquidity())))
                .recurring(encrypt(String.valueOf(request.recurring())))
                .monthlyInvest(request.monthlyInvest() > 0 ? encrypt(String.valueOf(request.monthlyInvest())) : null)
                .user(user)
                .budget(budget)
                .build();
    }

    /**
     * Updates the fields of an existing {@link Investment} entity based on the update request.
     *
     * @param investment the investment entity to update.
     * @param request    the investment update request containing new values.
     */
    private void updateInvestmentFields(Investment investment, UpdateInvestmentRequest request) {
        investment.setAsset(encrypt(request.asset()));
        investment.setAmount(encrypt(String.valueOf(request.amount())));
        investment.setCategory(encrypt(request.category()));
        investment.setState(encrypt(request.state()));
        investment.setLiquidity(encrypt(String.valueOf(request.liquidity())));
    }

    /**
     * Maps an {@link Investment} entity to an {@link InvestmentResponse}.
     *
     * @param investment the investment entity.
     * @return the corresponding {@link InvestmentResponse} with decrypted details.
     */
    private InvestmentResponse mapToResponse(Investment investment) {
        return new InvestmentResponse(
                investment.getId(),
                decrypt(investment.getAsset()),
                Integer.parseInt(decrypt(investment.getAmount())),
                decrypt(investment.getCategory()),
                decrypt(investment.getState()),
                Integer.parseInt(decrypt(investment.getLiquidity())),
                investment.getBudget().getId()
        );
    }

    /**
     * Encrypts the provided value using the {@link EncryptionService}.
     *
     * @param value the value to encrypt.
     * @return the encrypted value.
     */
    private String encrypt(String value) {
        return encryptionService.encrypt(value);
    }

    /**
     * Decrypts the provided value using the {@link EncryptionService}.
     *
     * @param value the value to decrypt.
     * @return the decrypted value.
     */
    private String decrypt(String value) {
        return encryptionService.decrypt(value);
    }
}
