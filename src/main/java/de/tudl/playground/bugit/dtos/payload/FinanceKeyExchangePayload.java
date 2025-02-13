package de.tudl.playground.bugit.dtos.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceKeyExchangePayload {
    // Eindeutige Kennung, z. B. "FINANCE_API"
    private String clientId;
    // Base64-kodierter Public Key der FinanceAPI
    private String clientPublicKey;
}
