package de.tudl.playground.bugit.dtos.requests.spending;

import java.time.LocalDate;

public record UpdateSpendingRequest(
        String spendingId,
        String name,
        Double amount,
        String category,
        LocalDate date,
        boolean isRecurring,
        String recurrenceInterval,
        LocalDate endDate
) {
}
