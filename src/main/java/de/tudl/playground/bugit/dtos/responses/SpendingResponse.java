package de.tudl.playground.bugit.dtos.responses;

import de.tudl.playground.bugit.models.enums.RecurrenceInterval;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpendingResponse
{
    private UUID spendingId;

    private String name;

    private Double amount;

    private String category;

    private LocalDate date;

    private boolean isRecurring;

    private RecurrenceInterval interval;

    private LocalDate endDate;

}
