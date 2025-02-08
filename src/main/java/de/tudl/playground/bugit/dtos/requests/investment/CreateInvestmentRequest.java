package de.tudl.playground.bugit.dtos.requests.investment;

public record CreateInvestmentRequest(
        String asset,
        int amount,
        String category,
        String state,
        int liquidity
) {
}
