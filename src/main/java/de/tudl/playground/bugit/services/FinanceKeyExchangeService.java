package de.tudl.playground.bugit.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tudl.playground.bugit.dtos.payload.FinanceKeyExchangePayload;
import de.tudl.playground.bugit.dtos.requests.influx.FinanceHybridKeyExchangeRequest;
import de.tudl.playground.bugit.dtos.responses.FinanceKeyExchangeResponse;
import de.tudl.playground.bugit.util.CryptoUtil;
import de.tudl.playground.bugit.util.HybridCryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.PrivateKey;
import java.security.PublicKey;

@Slf4j
@Service
public class FinanceKeyExchangeService implements ApplicationRunner {

    // InfluxAPI Public Key (Base64-kodiert)
    @Value("${influx.publicKey}")
    private String influxPublicKeyBase64;

    // FinanceAPI Private Key (Base64-kodiert) – zum Entschlüsseln der Antwort
    @Value("${finance.api.privateKey}")
    private String financePrivateKeyBase64;

    // FinanceAPI Public Key (Base64-kodiert) – wird in der Payload mitgesendet
    @Value("${finance.api.publicKey}")
    private String financePublicKeyBase64;

    // Eindeutige Client-ID der FinanceAPI (z. B. "FINANCE_API")
    @Value("${finance.clientId}")
    private String financeClientId;

    // URL des Key-Exchange-Endpunkts der InfluxAPI
    @Value("${influx.keyExchangeUrl}")
    private String influxKeyExchangeUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Hier wird der von der InfluxAPI zurückgegebene API-Key gespeichert
    private String influxApiKey;

    public FinanceKeyExchangeService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Erstelle den Payload mit Client-ID und FinanceAPI Public Key
        FinanceKeyExchangePayload payload = new FinanceKeyExchangePayload(financeClientId, financePublicKeyBase64);
        String payloadJson = objectMapper.writeValueAsString(payload);

        // Lade den InfluxAPI Public Key (zum Verschlüsseln des AES-Schlüssels)
        PublicKey influxPublicKey = CryptoUtil.getPublicKeyFromBase64(influxPublicKeyBase64);

        // Verwende den hybriden Verschlüsselungsansatz
        HybridCryptoUtil.HybridEncryptionResult hybridResult = HybridCryptoUtil.hybridEncrypt(payloadJson, influxPublicKey);

        // Erstelle den Request mit den drei Komponenten
        FinanceHybridKeyExchangeRequest request = new FinanceHybridKeyExchangeRequest(
                hybridResult.encryptedAesKey(),
                hybridResult.iv(),
                hybridResult.encryptedData()
        );

        // Sende die Anfrage an die InfluxAPI
        FinanceKeyExchangeResponse response = restTemplate.postForObject(influxKeyExchangeUrl, request, FinanceKeyExchangeResponse.class);

        if (response != null && response.getEncryptedApiKey() != null) {
            // Entschlüssele den erhaltenen API-Key mit dem FinanceAPI Private Key
            PrivateKey financePrivateKey = CryptoUtil.getPrivateKeyFromBase64(financePrivateKeyBase64);
            String decryptedApiKey = CryptoUtil.decryptRSA(response.getEncryptedApiKey(), financePrivateKey);
            this.influxApiKey = decryptedApiKey;
            log.info("Decrypted api key: {}", decryptedApiKey);
            // Speichere oder verwende den API-Key für spätere REST-Calls an die InfluxAPI.
        } else {
            log.error("Es wurde kein API-Key von der InfluxAPI zurückgegeben.");
        }
    }
}
