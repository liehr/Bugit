package de.tudl.playground.bugit.dtos.requests;

public record CreateInvestmentRequest(
        String asset,
        int amount,
        String category,
        String state,
        int liquidity
) {
}
