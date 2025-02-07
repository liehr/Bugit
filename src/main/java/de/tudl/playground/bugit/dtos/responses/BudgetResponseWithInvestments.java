package de.tudl.playground.bugit.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class BudgetResponseWithInvestments {
    private UUID id;

    private int amount;

    private List<InvestmentResponse> investments;
}
