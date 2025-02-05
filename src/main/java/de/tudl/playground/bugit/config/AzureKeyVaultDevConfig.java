package de.tudl.playground.bugit.config;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class AzureKeyVaultDevConfig {

    @Value("${AZURE_KEY_VAULT_URI}")
    private String keyVaultUri;

    @Value("${AZURE_CLIENT_ID}")
    private String clientId;

    @Value("${AZURE_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${AZURE_TENANT_ID}")
    private String tenantId;

    @Bean
    public SecretClient secretClient() {
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();

        return new SecretClientBuilder()
                .vaultUrl(keyVaultUri)
                .credential(credential)
                .buildClient();
    }
}

