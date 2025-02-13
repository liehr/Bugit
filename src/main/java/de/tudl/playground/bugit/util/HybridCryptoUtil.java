package de.tudl.playground.bugit.util;

import lombok.Getter;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;

public class HybridCryptoUtil {

    /**
     * Verschlüsselt die übergebene Zeichenkette mit AES und verschlüsselt den AES-Schlüssel mit RSA.
     *
     * @param data         Klartext, der verschlüsselt werden soll (z. B. JSON-Payload)
     * @param rsaPublicKey Der PublicKey der InfluxAPI (als RSA-Schlüssel), um den AES-Schlüssel zu verschlüsseln
     * @return Ein HybridEncryptionResult, das verschlüsselten AES-Schlüssel, IV und die mit AES verschlüsselte Payload enthält.
     * @throws Exception
     */
    public static HybridEncryptionResult hybridEncrypt(String data, PublicKey rsaPublicKey) throws Exception {
        // 1. Erzeuge einen zufälligen AES-Schlüssel (hier 128 Bit)
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey aesKey = keyGen.generateKey();

        // 2. Verschlüssele die Daten mit AES/CBC/PKCS5Padding
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] ivBytes = aesCipher.getIV();  // Initialisierungsvektor
        byte[] encryptedDataBytes = aesCipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // 3. Verschlüssele den AES-Schlüssel mit RSA/ECB/PKCS1Padding
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
        byte[] encryptedAesKeyBytes = rsaCipher.doFinal(aesKey.getEncoded());

        return new HybridEncryptionResult(
                Base64.getEncoder().encodeToString(encryptedAesKeyBytes),
                Base64.getEncoder().encodeToString(ivBytes),
                Base64.getEncoder().encodeToString(encryptedDataBytes)
        );
    }
        public record HybridEncryptionResult(String encryptedAesKey, String iv, String encryptedData) {
    }
}
