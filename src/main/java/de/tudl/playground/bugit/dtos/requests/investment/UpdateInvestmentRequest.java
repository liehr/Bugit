package de.tudl.playground.bugit.dtos.requests.investment;

public record UpdateInvestmentRequest(
        String investmentId,
        String asset,
        int amount,
        String category,
        String state,
        int liquidity
) {
}
