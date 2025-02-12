package de.tudl.playground.bugit.dtos.requests.spending;

import de.tudl.playground.bugit.models.enums.RecurrenceInterval;

import java.time.LocalDate;

public record CreateSpendingRequest(
        String name,
        Double amount,
        String category,
        LocalDate date,
        boolean isRecurring,
        RecurrenceInterval recurrenceInterval,
        LocalDate endDate,
        String note

) {
}
