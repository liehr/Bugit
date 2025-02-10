package de.tudl.playground.bugit.services;

import de.tudl.playground.bugit.dtos.requests.spending.CreateSpendingRequest;
import de.tudl.playground.bugit.dtos.responses.SpendingResponse;
import de.tudl.playground.bugit.exception.UnauthorizedException;
import de.tudl.playground.bugit.models.Spending;
import de.tudl.playground.bugit.models.User;
import de.tudl.playground.bugit.repositories.SpendingRepository;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SpendingService {

    private final SpendingRepository spendingRepository;
    private final EncryptionService encryptionService;
    private final AuthenticationService authenticationService;

    public SpendingService(SpendingRepository spendingRepository, EncryptionService encryptionService, AuthenticationService authenticationService) {
        this.spendingRepository = spendingRepository;
        this.encryptionService = encryptionService;
        this.authenticationService = authenticationService;
    }

    public SpendingResponse createSpending(CreateSpendingRequest request) {
        User user = getAuthenticatedUser();

        Spending spending = buildSpending(request, user);
        spendingRepository.save(spending);

        return mapToResponse(spending);
    }

    public List<SpendingResponse> getAllSpendingsByUser()
    {
        User user = getAuthenticatedUser();

        return spendingRepository.findAllSpendingsByUser(user)
                .map(spendings -> spendings.stream()
                        .map(this::mapToResponse)
                        .toList())
                .orElse(List.of());
    }

    @SneakyThrows
    private User getAuthenticatedUser() {
        return Optional.ofNullable(authenticationService.getCurrentUser())
                .orElseThrow(() -> new UnauthorizedException("User not authorized"));
    }

    private Spending buildSpending(CreateSpendingRequest request, User user) {
        return Spending.builder()
                .id(UUID.randomUUID())
                .name(encrypt(request.name()))
                .amount(encrypt(String.valueOf(request.amount())))
                .category(encrypt(request.category()))
                .date(request.date() != null ? encrypt(String.valueOf(request.date())) : null)
                .isRecurring(encrypt(String.valueOf(request.isRecurring())))
                .recurrenceInterval(request.recurrenceInterval())
                .endDate(request.endDate() != null ? encrypt(String.valueOf(request.endDate())) : null)
                .user(user)
                .build();
    }

    private SpendingResponse mapToResponse(Spending spending) {
        return new SpendingResponse(
                spending.getId(),
                decrypt(spending.getName()),
                Double.parseDouble(decrypt(spending.getAmount())),
                decrypt(spending.getCategory()),
                spending.getDate() != null ? decryptToLocalDate(spending.getDate()) : null,
                Boolean.parseBoolean(decrypt(spending.getIsRecurring())),
                spending.getRecurrenceInterval(),
                spending.getEndDate() != null ? decryptToLocalDate(spending.getEndDate()) : null
        );
    }

    private String encrypt(String value)
    {
        return encryptionService.encrypt(value);
    }

    private String decrypt(String value)
    {
        return encryptionService.decrypt(value);
    }

    private LocalDate decryptToLocalDate(String value)
    {
        return LocalDate.parse(encryptionService.decrypt(value));
    }
}
