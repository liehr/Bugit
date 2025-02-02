package de.tudl.playground.bugit.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class BudgetResponse {
    private UUID id;

    private int amount;
}
