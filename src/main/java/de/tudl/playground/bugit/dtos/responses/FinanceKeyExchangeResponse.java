package de.tudl.playground.bugit.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceKeyExchangeResponse {
    private String encryptedApiKey;
}

