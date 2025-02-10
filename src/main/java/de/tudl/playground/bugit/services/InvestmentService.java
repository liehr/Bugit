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

@Service
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final AuthenticationService authenticationService;
    private final EncryptionService encryptionService;
    private final BudgetRepository budgetRepository;

    public InvestmentService(InvestmentRepository investmentRepository, AuthenticationService authenticationService,
                             EncryptionService encryptionService, BudgetRepository budgetRepository) {
        this.investmentRepository = investmentRepository;
        this.authenticationService = authenticationService;
        this.encryptionService = encryptionService;
        this.budgetRepository = budgetRepository;
    }

    @SneakyThrows
    public InvestmentResponse createInvestment(CreateInvestmentRequest request) {
        User user = getAuthenticatedUser();
        Budget budget = budgetRepository.findBudgetByUser(user)
                .orElseThrow(() -> new IllegalStateException("No budget found for the current user."));

        Investment investment = buildInvestment(request, user, budget);
        investmentRepository.save(investment);

        return mapToResponse(investment);
    }

    @SneakyThrows
    public List<InvestmentResponse> getAllInvestmentsByUser() {
        User user = getAuthenticatedUser();
        return investmentRepository.findAllByUser(user)
                .map(investments -> investments.stream()
                        .map(this::mapToResponse)
                        .toList())
                .orElse(List.of());
    }

    @SneakyThrows
    public InvestmentResponse updateInvestment(UpdateInvestmentRequest request) {
        User user = getAuthenticatedUser();
        Investment investment = getInvestmentByIdAndUser(request.investmentId(), user);

        updateInvestmentFields(investment, request);
        investmentRepository.save(investment);

        return mapToResponse(investment);
    }

    @SneakyThrows
    public String deleteInvestment(DeleteInvestmentRequest request) {
        User user = getAuthenticatedUser();
        Investment investment = getInvestmentByIdAndUser(request.investmentId(), user);

        investmentRepository.delete(investment);
        return "SUCCESS";
    }

    // --- Helper Methods ---

    @SneakyThrows
    private User getAuthenticatedUser() {
        return Optional.ofNullable(authenticationService.getCurrentUser())
                .orElseThrow(() -> new UnauthorizedException("User not authorized!"));
    }

    private Investment getInvestmentByIdAndUser(String investmentId, User user) {
        return investmentRepository.findById(UUID.fromString(investmentId))
                .filter(inv -> inv.getUser().equals(user))
                .orElseThrow(() -> new IllegalStateException("Investment not found or unauthorized."));
    }

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

    private void updateInvestmentFields(Investment investment, UpdateInvestmentRequest request) {
        investment.setAsset(encrypt(request.asset()));
        investment.setAmount(encrypt(String.valueOf(request.amount())));
        investment.setCategory(encrypt(request.category()));
        investment.setState(encrypt(request.state()));
        investment.setLiquidity(encrypt(String.valueOf(request.liquidity())));
    }

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

    private String encrypt(String value) {
        return encryptionService.encrypt(value);
    }

    private String decrypt(String value) {
        return encryptionService.decrypt(value);
    }
}
