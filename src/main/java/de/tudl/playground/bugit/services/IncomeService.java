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

/**
 * Service class for managing income operations.
 * <p>
 * This service provides methods to create, retrieve, update, and delete income records
 * associated with the currently authenticated user. Income details (source and amount) are
 * stored in encrypted form using an {@link EncryptionService}.
 * </p>
 */
@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final AuthenticationService authenticationService;
    private final EncryptionService encryptionService;

    /**
     * Constructs a new IncomeService with the required dependencies.
     *
     * @param incomeRepository      repository for income data.
     * @param authenticationService service for retrieving the currently authenticated user.
     * @param encryptionService     service for encryption and decryption.
     */
    public IncomeService(IncomeRepository incomeRepository,
                         AuthenticationService authenticationService,
                         EncryptionService encryptionService) {
        this.incomeRepository = incomeRepository;
        this.authenticationService = authenticationService;
        this.encryptionService = encryptionService;
    }

    /**
     * Creates a new income record for the authenticated user.
     *
     * @param request the request containing income details.
     * @return the created income represented as an {@link IncomeResponse}.
     */
    public IncomeResponse create(CreateIncomeRequest request) {
        return Optional.of(getAuthenticatedUser())
                .map(user -> new Income(
                        UUID.randomUUID(),
                        encrypt(request.source()),
                        encrypt(String.valueOf(request.amount())),
                        user))
                .map(incomeRepository::save)
                .map(this::mapToResponse)
                .orElseThrow();
    }

    /**
     * Retrieves all income records associated with the authenticated user.
     *
     * @return a list of incomes as {@link IncomeResponse}; if no incomes exist, an empty list is returned.
     */
    public List<IncomeResponse> getAllIncomesByUser() {
        return incomeRepository.findIncomesByUser(getAuthenticatedUser())
                .orElse(List.of())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Updates an existing income record for the authenticated user.
     *
     * @param request the request containing the income ID and new details.
     * @return the updated income as an {@link IncomeResponse}, or {@code null} if not found or unauthorized.
     */
    public IncomeResponse update(UpdateIncomeRequest request) {
        return incomeRepository.findById(UUID.fromString(request.incomeId()))
                .filter(income -> income.getUser().equals(getAuthenticatedUser()))
                .map(income -> {
                    income.setSource(encrypt(request.source()));
                    income.setAmount(encrypt(String.valueOf(request.amount())));
                    return incomeRepository.save(income);
                })
                .map(this::mapToResponse)
                .orElse(null);
    }

    /**
     * Deletes an existing income record for the authenticated user.
     *
     * @param request the request containing the income ID to delete.
     * @return "SUCCESS" if the deletion was successful, or {@code null} if the record was not found or unauthorized.
     */
    public String delete(DeleteIncomeRequest request) {
        return incomeRepository.findById(UUID.fromString(request.incomeId()))
                .filter(income -> income.getUser().equals(getAuthenticatedUser()))
                .map(income -> {
                    incomeRepository.delete(income);
                    return "SUCCESS";
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
     * Maps an {@link Income} entity to an {@link IncomeResponse}.
     *
     * @param income the income entity.
     * @return the corresponding {@link IncomeResponse} with decrypted details.
     */
    private IncomeResponse mapToResponse(Income income) {
        return new IncomeResponse(
                income.getId(),
                decrypt(income.getSource()),
                Double.parseDouble(decrypt(income.getAmount()))
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
