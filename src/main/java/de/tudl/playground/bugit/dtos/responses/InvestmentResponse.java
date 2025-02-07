package de.tudl.playground.bugit.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class InvestmentResponse {
    private UUID investmentId;
    private String asset;
    private int amount;
    private String category;
    private String state;
    private int liquidity;
    private UUID budgetId;
}
