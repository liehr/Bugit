package de.tudl.playground.bugit.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class IncomeResponse
{
    private String source;

    private double amount;
}
