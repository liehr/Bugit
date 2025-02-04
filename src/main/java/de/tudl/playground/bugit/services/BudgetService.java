package de.tudl.playground.bugit.services;

import de.tudl.playground.bugit.dtos.requests.CreateBudgetRequest;
import de.tudl.playground.bugit.dtos.requests.DeleteBudgetRequest;
import de.tudl.playground.bugit.dtos.requests.UpdateBudgetRequest;
import de.tudl.playground.bugit.dtos.responses.BudgetResponse;
import de.tudl.playground.bugit.models.Budget;
import de.tudl.playground.bugit.models.User;
import de.tudl.playground.bugit.repositories.BudgetRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final AuthenticationService authenticationService;

    public BudgetService(BudgetRepository budgetRepository, AuthenticationService authenticationService) {
        this.budgetRepository = budgetRepository;
        this.authenticationService = authenticationService;
    }

    public BudgetResponse createBudget(CreateBudgetRequest request) {
        User user = authenticationService.getCurrentUser();

        Budget budget = new Budget();
        budget.setId(UUID.randomUUID());
        budget.setAmount(request.amount());
        budget.setUser(user);

        budgetRepository.save(budget);

        return new BudgetResponse(budget.getId(), budget.getAmount());
    }

    public BudgetResponse getBudgetByUser() {
        User currentUser = authenticationService.getCurrentUser();

        if (currentUser == null) {
            return null;
        }

        return budgetRepository.findBudgetByUser(currentUser)
                .filter(budget -> currentUser.equals(budget.getUser()))
                .map(budget -> new BudgetResponse(budget.getId(), budget.getAmount()))
                .orElse(null);
    }

    public BudgetResponse updateBudget(UpdateBudgetRequest request) {
        User currentUser = authenticationService.getCurrentUser();

        if (currentUser == null) {
            return null;
        }

        return budgetRepository.findById(UUID.fromString(request.budgetId()))
                .filter(budget -> currentUser.equals(budget.getUser()))
                .map(budget -> {
                    budget.setAmount(request.amount());
                    Budget updatedBudget = budgetRepository.save(budget);
                    return new BudgetResponse(updatedBudget.getId(), updatedBudget.getAmount());
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
}
