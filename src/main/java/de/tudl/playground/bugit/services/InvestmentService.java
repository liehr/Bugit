package de.tudl.playground.bugit.services;

import de.tudl.playground.bugit.dtos.requests.CreateInvestmentRequest;
import de.tudl.playground.bugit.dtos.responses.InvestmentResponse;
import de.tudl.playground.bugit.exception.UnauthorizedException;
import de.tudl.playground.bugit.models.Budget;
import de.tudl.playground.bugit.models.Investment;
import de.tudl.playground.bugit.models.User;
import de.tudl.playground.bugit.repositories.BudgetRepository;
import de.tudl.playground.bugit.repositories.IncomeRepository;
import de.tudl.playground.bugit.repositories.InvestmentRepository;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final AuthenticationService authenticationService;
    private final EncryptionService encryptionService;
    private final BudgetRepository budgetRepository;

    public InvestmentService(InvestmentRepository investmentRepository, AuthenticationService authenticationService, EncryptionService encryptionService, BudgetRepository budgetRepository) {
        this.investmentRepository = investmentRepository;
        this.authenticationService = authenticationService;
        this.encryptionService = encryptionService;
        this.budgetRepository = budgetRepository;
    }

    @SneakyThrows
    public InvestmentResponse createInvestment(CreateInvestmentRequest request) {
        User user = authenticationService.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("User not authorized!");
        }

        Budget budget = budgetRepository
                .findBudgetByUser(user)
                .orElseThrow(() -> new IllegalStateException("No budget found for the current user."));

        Investment investment = new Investment();
        investment.setId(UUID.randomUUID());
        investment.setAsset(encryptionService.encrypt(request.asset()));
        investment.setAmount(encryptionService.encrypt(String.valueOf(request.amount())));
        investment.setCategory(encryptionService.encrypt(request.category()));
        investment.setState(encryptionService.encrypt(request.state()));
        investment.setLiquidity(encryptionService.encrypt(String.valueOf(request.liquidity())));

        investment.setUser(user);
        investment.setBudget(budget);

        investmentRepository.save(investment);

        return mapToResponse(investment);
    }

    @SneakyThrows
    public List<InvestmentResponse> getAllInvestmentsByUser() {
        User user = authenticationService.getCurrentUser();

        if (user == null) {
            throw new UnauthorizedException("User not authorized!");
        }

        return investmentRepository.findAllByUser(user)
                .map(investments -> investments.stream()
                        .map(this::mapToResponse)
                        .toList())
                .orElseGet(Collections::emptyList);
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
