package de.tudl.playground.bugit.services;

import de.tudl.playground.bugit.dtos.requests.income.CreateIncomeRequest;
import de.tudl.playground.bugit.dtos.requests.income.DeleteIncomeRequest;
import de.tudl.playground.bugit.dtos.requests.income.UpdateIncomeRequest;
import de.tudl.playground.bugit.dtos.responses.IncomeResponse;
import de.tudl.playground.bugit.exception.UnauthorizedException;
import de.tudl.playground.bugit.models.Income;
import de.tudl.playground.bugit.models.User;
import de.tudl.playground.bugit.repositories.IncomeRepository;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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

    public IncomeResponse create(CreateIncomeRequest request) {
        User user = getAuthenticatedUser();
        Income income = new Income(UUID.randomUUID(), encrypt(request.source()), encrypt(String.valueOf(request.amount())), user);
        incomeRepository.save(income);
        return mapToResponse(income);
    }

    public List<IncomeResponse> getAllIncomesByUser() {
        return incomeRepository.findIncomesByUser(getAuthenticatedUser())
                .orElse(List.of())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public IncomeResponse update(UpdateIncomeRequest request) {
        return incomeRepository.findById(UUID.fromString(request.incomeId()))
                .filter(income -> income.getUser().equals(getAuthenticatedUser()))
                .map(income -> {
                    income.setSource(encrypt(request.source()));
                    income.setAmount(encrypt(String.valueOf(request.amount())));
                    incomeRepository.save(income);
                    return mapToResponse(income);
                })
                .orElse(null);
    }

    public String delete(DeleteIncomeRequest request) {
        return incomeRepository.findById(UUID.fromString(request.incomeId()))
                .filter(income -> income.getUser().equals(getAuthenticatedUser()))
                .map(income -> {
                    incomeRepository.delete(income);
                    return "SUCCESS";
                })
                .orElse(null);
    }

    @SneakyThrows
    private User getAuthenticatedUser() {
        return Optional.ofNullable(authenticationService.getCurrentUser())
                .orElseThrow(() -> new UnauthorizedException("User not authorized!"));
    }

    private IncomeResponse mapToResponse(Income income) {
        return new IncomeResponse(
                income.getId(),
                decrypt(income.getSource()),
                Double.parseDouble(decrypt(income.getAmount()))
        );
    }

    private String encrypt(String value) {
        return encryptionService.encrypt(value);
    }

    private String decrypt(String value) {
        return encryptionService.decrypt(value);
    }
}
