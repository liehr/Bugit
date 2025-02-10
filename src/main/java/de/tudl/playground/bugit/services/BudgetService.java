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

@Service
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final AuthenticationService authenticationService;
    private final EncryptionService encryptionService;

    public BudgetService(BudgetRepository budgetRepository, AuthenticationService authenticationService, EncryptionService encryptionService) {
        this.budgetRepository = budgetRepository;
        this.authenticationService = authenticationService;
        this.encryptionService = encryptionService;
    }

    public BudgetResponse createBudget(CreateBudgetRequest request) {
        User user = getAuthenticatedUser();
        Budget budget = new Budget();
        budget.setId(UUID.randomUUID());
        budget.setAmount(encrypt(String.valueOf(request.amount())));
        budget.setUser(user);

        budgetRepository.save(budget);

        return mapToResponse(budget);
    }

    public BudgetResponse getBudgetByUser() {
        User authenticatedUser = getAuthenticatedUser();
        return budgetRepository.findBudgetByUser(authenticatedUser)
                .map(this::mapToResponse)
                .orElseGet(() -> {
                    Budget newBudget = new Budget();
                    newBudget.setUser(authenticatedUser);
                    newBudget.setAmount(encrypt(String.valueOf(5)));
                    newBudget.setId(UUID.randomUUID());
                    newBudget = budgetRepository.save(newBudget);
                    return mapToResponse(newBudget);
                });
    }


    @SneakyThrows
    public BudgetResponseWithInvestments getBudgetWithInvestments() {
        Budget budget = budgetRepository.findBudgetWithInvestmentsByUser(getAuthenticatedUser())
                .orElseThrow(() -> new IllegalStateException("No budget found for user"));

        List<InvestmentResponse> investments = budget.getInvestments().stream()
                .map(this::mapInvestmentToResponse)
                .toList();

        return new BudgetResponseWithInvestments(budget.getId(), decryptToInt(budget.getAmount()), investments);
    }

    public BudgetResponse updateBudget(UpdateBudgetRequest request) {
        return budgetRepository.findById(UUID.fromString(request.budgetId()))
                .filter(budget -> budget.getUser().equals(getAuthenticatedUser()))
                .map(budget -> {
                    budget.setAmount(encrypt(String.valueOf(request.amount())));
                    budgetRepository.save(budget);
                    return mapToResponse(budget);
                })
                .orElse(null);
    }

    public String deleteBudget(DeleteBudgetRequest request) {
        return budgetRepository.findById(UUID.fromString(request.budgetId()))
                .filter(budget -> budget.getUser().equals(getAuthenticatedUser()))
                .map(budget -> {
                    budgetRepository.delete(budget);
                    return "Success";
                })
                .orElse(null);
    }

    @SneakyThrows
    private User getAuthenticatedUser() {
        return Optional.ofNullable(authenticationService.getCurrentUser())
                .orElseThrow(() -> new UnauthorizedException("User not authorized!"));
    }

    private BudgetResponse mapToResponse(Budget budget) {
        return new BudgetResponse(budget.getId(), decryptToInt(budget.getAmount()));
    }

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

    private String encrypt(String value) {
        return encryptionService.encrypt(value);
    }

    private String decrypt(String value) {
        return encryptionService.decrypt(value);
    }

    private int decryptToInt(String value) {
        return Integer.parseInt(decrypt(value));
    }
}
