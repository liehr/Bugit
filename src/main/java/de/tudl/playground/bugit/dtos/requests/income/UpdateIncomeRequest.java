package de.tudl.playground.bugit.dtos.requests.income;

public record UpdateIncomeRequest(String incomeId, String source, double amount) {

}
