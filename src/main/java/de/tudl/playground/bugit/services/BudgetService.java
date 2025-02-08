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
import java.util.NoSuchElementException;
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
        User user = authenticationService.getCurrentUser();

        Budget budget = new Budget();
        budget.setId(UUID.randomUUID());
        budget.setAmount(encryptionService.encrypt(String.valueOf(request.amount())));
        budget.setUser(user);

        budgetRepository.save(budget);

        return new BudgetResponse(budget.getId(), Integer.parseInt(encryptionService.decrypt(budget.getAmount())));
    }

    public BudgetResponse getBudgetByUser() {
        User currentUser = authenticationService.getCurrentUser();

        if (currentUser == null) {
            return null;
        }

        return budgetRepository.findBudgetByUser(currentUser)
                .filter(budget -> currentUser.equals(budget.getUser()))
                .map(budget -> new BudgetResponse(budget.getId(), Integer.parseInt(encryptionService.decrypt(budget.getAmount()))))
                .orElse(null);
    }

    @SneakyThrows
    public BudgetResponseWithInvestments getBudgetWithInvestments() {
        User currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedException("User not authorized!");
        }

        Optional<Budget> budgetsWithInvestments = budgetRepository.findBudgetWithInvestmentsByUser(currentUser);

        if (budgetsWithInvestments.isPresent()) {
            Budget budget = budgetsWithInvestments.get();

            String decryptedAmount = encryptionService.decrypt(budget.getAmount());
            int amountAsInt = Integer.parseInt(decryptedAmount);

            List<InvestmentResponse> investmentResponses = budget.getInvestments().stream()
                    .map(this::mapToResponse)
                    .toList();

            return
                    new BudgetResponseWithInvestments(
                    budget.getId(),
                    amountAsInt,
                    investmentResponses
            );
        } else {
            throw new NoSuchElementException("No budget found for user: " + currentUser.getUsername());
        }
    }


    public BudgetResponse updateBudget(UpdateBudgetRequest request) {
        User currentUser = authenticationService.getCurrentUser();

        if (currentUser == null) {
            return null;
        }

        return budgetRepository.findById(UUID.fromString(request.budgetId()))
                .filter(budget -> currentUser.equals(budget.getUser()))
                .map(budget -> {
                    budget.setAmount(encryptionService.encrypt(String.valueOf(request.amount())));
                    Budget updatedBudget = budgetRepository.save(budget);
                    return new BudgetResponse(updatedBudget.getId(), Integer.parseInt(encryptionService.decrypt(updatedBudget.getAmount())));
                })
                .orElse(null);
    }

    public String deleteBudget(DeleteBudgetRequest request)
    {
        User currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            return null;
        }

        return budgetRepository.findById(UUID.fromString(request.budgetId()))
                .filter(budget -> currentUser.equals(budget.getUser()))
                .map(budget -> {
                    budgetRepository.delete(budget);
                    return "Success";
                })
                .orElse(null);
    }

    protected InvestmentResponse mapToResponse(Investment investment) {
        return new InvestmentResponse(
                investment.getId(),
                encryptionService.decrypt(investment.getAsset()),
                Integer.parseInt(encryptionService.decrypt(investment.getAmount())),
                encryptionService.decrypt(investment.getCategory()),
                encryptionService.decrypt(investment.getState()),
                Integer.parseInt(encryptionService.decrypt(investment.getLiquidity())),
                investment.getBudget().getId()
        );
    }
}
