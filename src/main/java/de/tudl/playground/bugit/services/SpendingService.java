package de.tudl.playground.bugit.services;

import de.tudl.playground.bugit.dtos.requests.spending.CreateSpendingRequest;
import de.tudl.playground.bugit.dtos.requests.spending.DeleteSpendingRequest;
import de.tudl.playground.bugit.dtos.requests.spending.UpdateSpendingRequest;
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

/**
 * Service class for managing spending operations.
 * <p>
 * This service provides methods to create, update, delete, and retrieve spending records
 * associated with the currently authenticated user. Spending details are stored in encrypted
 * form using an {@link EncryptionService}.
 * </p>
 */
@Service
public class SpendingService {

    private final SpendingRepository spendingRepository;
    private final EncryptionService encryptionService;
    private final AuthenticationService authenticationService;

    /**
     * Constructs a new SpendingService with the required dependencies.
     *
     * @param spendingRepository    repository for spending data.
     * @param encryptionService     service for encryption and decryption.
     * @param authenticationService service for retrieving the currently authenticated user.
     */
    public SpendingService(SpendingRepository spendingRepository, EncryptionService encryptionService, AuthenticationService authenticationService) {
        this.spendingRepository = spendingRepository;
        this.encryptionService = encryptionService;
        this.authenticationService = authenticationService;
    }

    /**
     * Creates a new spending record for the authenticated user.
     *
     * @param request the request containing spending details.
     * @return the created spending represented as a {@link SpendingResponse}.
     * @throws UnauthorizedException if the user is not authenticated.
     */
    public SpendingResponse createSpending(CreateSpendingRequest request) {
        return Optional.of(getAuthenticatedUser())
                .map(user -> buildSpending(request, user))
                .map(spendingRepository::save)
                .map(this::mapToResponse)
                .orElseThrow();
    }

    /**
     * Updates an existing spending record for the authenticated user.
     *
     * @param request the request containing the spending ID and updated details.
     * @return the updated spending represented as a {@link SpendingResponse}.
     * @throws IllegalStateException if the spending record is not found or the user is unauthorized.
     * @throws UnauthorizedException if the user is not authenticated.
     */
    public SpendingResponse updateSpending(UpdateSpendingRequest request) {
        return Optional.of(getAuthenticatedUser())
                .map(user -> getSpendingByIdAndUser(request.spendingId(), user))
                .map(spending -> {
                    updateSpendingFields(spending, request);
                    return spendingRepository.save(spending);
                })
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalStateException("Spending record not found or unauthorized."));
    }

    /**
     * Deletes an existing spending record for the authenticated user.
     *
     * @param request the request containing the spending ID to delete.
     * @return "SUCCESS" if the deletion was successful.
     * @throws IllegalStateException if the spending record is not found or the user is unauthorized.
     * @throws UnauthorizedException if the user is not authenticated.
     */
    @SneakyThrows
    public String deleteSpending(DeleteSpendingRequest request) {
        return Optional.of(getAuthenticatedUser())
                .map(user -> getSpendingByIdAndUser(request.spendingId(), user))
                .map(spending -> {
                    spendingRepository.delete(spending);
                    return "SUCCESS";
                })
                .orElseThrow(() -> new IllegalStateException("Spending record not found or unauthorized."));
    }

    /**
     * Retrieves all spending records associated with the authenticated user.
     *
     * @return a list of {@link SpendingResponse}; if no spending records are found, an empty list is returned.
     * @throws UnauthorizedException if the user is not authenticated.
     */
    public List<SpendingResponse> getAllSpendingsByUser() {
        User user = getAuthenticatedUser();
        return spendingRepository.findAllSpendingsByUser(user)
                .orElse(List.of())
                .stream()
                .map(this::mapToResponse)
                .toList();
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
                .orElseThrow(() -> new UnauthorizedException("User not authorized"));
    }

    /**
     * Retrieves a spending record by its ID for the given user.
     *
     * @param id   the spending ID as a string.
     * @param user the authenticated user.
     * @return the corresponding {@link Spending} record.
     * @throws IllegalStateException if the spending record is not found or does not belong to the user.
     */
    private Spending getSpendingByIdAndUser(String id, User user) {
        return spendingRepository.findById(UUID.fromString(id))
                .filter(spending -> spending.getUser().equals(user))
                .orElseThrow(() -> new IllegalStateException("Spending with id " + id + " not found or unauthorized."));
    }

    /**
     * Builds a {@link Spending} entity from the given creation request and user.
     *
     * @param request the creation request containing spending details.
     * @param user    the authenticated user.
     * @return a new {@link Spending} entity.
     */
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

    /**
     * Updates the fields of an existing {@link Spending} entity based on the update request.
     *
     * @param spending the spending entity to update.
     * @param request  the update request containing new values.
     */
    private void updateSpendingFields(Spending spending, UpdateSpendingRequest request) {
        spending.setName(encrypt(request.name()));
        spending.setAmount(encrypt(String.valueOf(request.amount())));
        spending.setCategory(encrypt(request.category()));
        spending.setRecurrenceInterval(request.recurrenceInterval());
        spending.setDate(encrypt(String.valueOf(request.date())));
        spending.setEndDate(encrypt(String.valueOf(request.endDate())));
        spending.setIsRecurring(encrypt(String.valueOf(request.isRecurring())));
    }

    /**
     * Maps a {@link Spending} entity to a {@link SpendingResponse} with decrypted details.
     *
     * @param spending the spending entity.
     * @return the corresponding {@link SpendingResponse}.
     */
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

    /**
     * Decrypts the provided value to a {@link LocalDate} using the {@link EncryptionService}.
     *
     * @param value the encrypted date value.
     * @return the decrypted {@link LocalDate}.
     */
    private LocalDate decryptToLocalDate(String value) {
        return LocalDate.parse(decrypt(value));
    }
}
