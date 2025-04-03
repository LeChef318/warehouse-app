package com.warehouse.config;

import com.warehouse.model.User;
import com.warehouse.repository.UserRepository;
import com.warehouse.service.KeycloakService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2) // Run after database initialization
public class InitialAdminSetup implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(InitialAdminSetup.class);

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.keycloak.enabled:true}")
    private boolean keycloakEnabled;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakService keycloakService;

    @Autowired
    public InitialAdminSetup(UserRepository userRepository,
                             PasswordEncoder passwordEncoder,
                             KeycloakService keycloakService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.keycloakService = keycloakService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if any MANAGER users exist
        boolean managerExists = userRepository.findAll().stream()
                .anyMatch(user -> "MANAGER".equals(user.getRole()));

        if (!managerExists) {
            logger.info("No MANAGER users found. Creating initial admin user: {}", adminUsername);

            // Create admin user in Keycloak if enabled
            if (keycloakEnabled) {
                try {
                    boolean keycloakUserCreated = keycloakService.createKeycloakUser(
                            adminUsername,
                            adminPassword,
                            "MANAGER"
                    );

                    if (!keycloakUserCreated) {
                        logger.warn("Failed to create admin user in Keycloak. Continuing with database creation only.");
                    } else {
                        logger.info("Admin user created in Keycloak successfully.");
                    }
                } catch (Exception e) {
                    logger.error("Error creating admin user in Keycloak: {}", e.getMessage(), e);
                    logger.warn("Continuing with database creation only.");
                }
            }

            // Create admin user in database
            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setRole("MANAGER");

            try {
                userRepository.save(adminUser);
                logger.info("Admin user created in database successfully.");
            } catch (Exception e) {
                logger.error("Error creating admin user in database: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to create initial admin user", e);
            }
        } else {
            logger.info("MANAGER users already exist. Skipping initial admin setup.");
        }
    }
}

