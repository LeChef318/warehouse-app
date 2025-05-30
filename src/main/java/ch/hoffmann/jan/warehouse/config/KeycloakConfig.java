package ch.hoffmann.jan.warehouse.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakConfig.class);

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    @Bean
    public Keycloak keycloak() {
        logger.info("Initializing Keycloak client with server URL: {}", serverUrl);

        try {
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm("master")
                    .clientId("admin-cli")
                    .username(adminUsername)
                    .password(adminPassword)
                    .build();

            // Test the connection
            keycloak.serverInfo().getInfo();
            logger.info("Successfully connected to Keycloak server");

            return keycloak;
        } catch (Exception e) {
            logger.error("Failed to initialize Keycloak client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Keycloak client", e);
        }
    }
}