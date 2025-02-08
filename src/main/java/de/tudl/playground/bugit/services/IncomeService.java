package de.tudl.playground.bugit.services;

import de.tudl.playground.bugit.dtos.requests.income.CreateIncomeRequest;
import de.tudl.playground.bugit.dtos.requests.income.DeleteIncomeRequest;
import de.tudl.playground.bugit.dtos.requests.income.UpdateIncomeRequest;
import de.tudl.playground.bugit.dtos.responses.IncomeResponse;
import de.tudl.playground.bugit.models.Income;
import de.tudl.playground.bugit.models.User;
import de.tudl.playground.bugit.repositories.IncomeRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final AuthenticationService authenticationService;
    private final EncryptionService encryptionService;

    public IncomeService(IncomeRepository incomeRepository, AuthenticationService authenticationService, EncryptionService encryptionService) {
        this.incomeRepository = incomeRepository;
        this.authenticationService = authenticationService;
        this.encryptionService = encryptionService;
    }

    /**
     * Creates an Income record for the current user.
     *
     * @param request the income creation request.
     * @return the response DTO containing the income details.
     */
    public IncomeResponse create(CreateIncomeRequest request) {
        User currentUser = authenticationService.getCurrentUser();

        Income income = new Income();
        income.setId(UUID.randomUUID());
        income.setSource(encryptionService.encrypt(request.source()));
        income.setAmount(encryptionService.encrypt(String.valueOf(request.amount())));
        income.setUser(currentUser);

        incomeRepository.save(income);

        String decryptedSource = encryptionService.decrypt(income.getSource());
        double decryptedAmount = Double.parseDouble(encryptionService.decrypt(income.getAmount()));
        return new IncomeResponse(income.getId(), decryptedSource, decryptedAmount);
    }



    /**
     * Retrieves all incomes associated with the currently authenticated user.
     *
     * @return a list of IncomeResponse DTOs.
     */
    public List<IncomeResponse> getAllIncomesByUser() {
        User currentUser = authenticationService.getCurrentUser();

        List<Income> incomes = incomeRepository.findIncomesByUser(currentUser)
                .orElse(Collections.emptyList());

        return incomes.stream()
                .map(income ->
                {
                    String decryptedSource = encryptionService.decrypt(income.getSource());
                    double decryptedAmount = Double.parseDouble(encryptionService.decrypt(income.getAmount()));
                    return new IncomeResponse(income.getId(), decryptedSource, decryptedAmount);
                })
                .toList();
    }

    public IncomeResponse update(UpdateIncomeRequest request) {
        User currentUser = authenticationService.getCurrentUser();

        if (currentUser == null) {
            return null;
        }

        return incomeRepository.findById(UUID.fromString(request.incomeId()))
                .filter(income -> currentUser.equals(income.getUser()))
                .map(income -> {
                    income.setSource(encryptionService.encrypt(request.source()));
                    income.setAmount(encryptionService.encrypt(String.valueOf(request.amount())));
                    Income savedIncome = incomeRepository.save(income);

                    String decryptedSource = encryptionService.decrypt(income.getSource());
                    double decryptedAmount = Double.parseDouble(encryptionService.decrypt(income.getAmount()));
                    return new IncomeResponse(savedIncome.getId(), decryptedSource, decryptedAmount);
                })
                .orElse(null);
    }

    public String delete(DeleteIncomeRequest request) {

        User currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            return null;
        }

        return incomeRepository.findById(UUID.fromString(request.incomeId()))
                .filter(income -> currentUser.equals(income.getUser()))
                .map(income -> {
                    incomeRepository.delete(income);
                    return "SUCCESS";
                })
                .orElse(null);
    }
}
