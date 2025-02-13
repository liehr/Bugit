package de.tudl.playground.bugit.dtos.requests.influx;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceHybridKeyExchangeRequest {
    // Der AES-Schlüssel, verschlüsselt mit dem InfluxAPI PublicKey
    private String encryptedAesKey;
    // Der Initialisierungsvektor (IV) der AES-Verschlüsselung
    private String iv;
    // Die Payload, verschlüsselt mit AES
    private String encryptedData;
}

