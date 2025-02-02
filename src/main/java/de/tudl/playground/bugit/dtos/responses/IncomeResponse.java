package de.tudl.playground.bugit.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class IncomeResponse
{
    private UUID id;

    private String source;

    private double amount;
}
