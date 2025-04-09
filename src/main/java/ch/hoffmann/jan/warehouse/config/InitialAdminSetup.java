package ch.hoffmann.jan.warehouse.config;

import ch.hoffmann.jan.warehouse.model.User;
import ch.hoffmann.jan.warehouse.repository.UserRepository;
import ch.hoffmann.jan.warehouse.service.KeycloakService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Component responsible for setting up the initial admin user during application startup.
 * This runs after database initialization but before other application components.
 */
@Component
@Order(3)
public class InitialAdminSetup implements ApplicationRunner {

    private final KeycloakService keycloakService;
    private final UserRepository userRepository;
    private final ApplicationContext context;
    private final Logger logger = LoggerFactory.getLogger(InitialAdminSetup.class);

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.firstName}")
    private String adminFirstName;

    @Value("${app.admin.lastName}")
    private String adminLastName;

    public InitialAdminSetup(
            KeycloakService keycloakService,
            UserRepository userRepository,
            ApplicationContext context) {
        this.keycloakService = keycloakService;
        this.userRepository = userRepository;
        this.context = context;
    }

    /**
     * Validates that all required properties are set
     */
    @PostConstruct
    public void validateProperties() {
        logger.debug("Validating admin setup properties");
        if (adminUsername == null || adminUsername.isEmpty()) {
            logger.error("Admin username not configured. Set the app.admin.username property.");
            throw new IllegalStateException("Admin username not configured");
        }

        if (adminPassword == null || adminPassword.isEmpty()) {
            logger.error("Admin password not configured. Set the app.admin.password property.");
            throw new IllegalStateException("Admin password not configured");
        }

        logger.debug("Admin setup properties validated successfully");
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            logger.info("Starting initial admin setup...");

            // Verify Keycloak is available
            if (!keycloakService.isKeycloakAvailable()) {
                logger.error("Keycloak is not available - cannot proceed with admin setup");
                failApplication();
                return;
            }

            // Verify required roles exist
            if (!keycloakService.verifyRequiredRoles()) {
                logger.error("Required roles do not exist in Keycloak - cannot proceed with admin setup");
                failApplication();
                return;
            }

            // Check if ANY manager exists in our database
            boolean managerExists = userRepository.existsByRole("MANAGER");
            if (managerExists) {
                logger.info("A manager user already exists, skipping initial admin setup");
                return;
            }

            // Check if admin exists in Keycloak
            boolean adminExistsInKeycloak = keycloakService.userExistsByUsername(adminUsername);
            logger.info("Admin user exists in Keycloak: {}", adminExistsInKeycloak);

            String keycloakId = null;
            boolean newUserCreated = false;

            if (adminExistsInKeycloak) {
                // Get the Keycloak ID of existing user
                keycloakId = keycloakService.getUserIdByUsername(adminUsername);
                if (keycloakId == null) {
                    logger.error("Failed to get Keycloak ID for existing admin user");
                    failApplication();
                    return;
                }
                logger.info("Found existing admin user in Keycloak with ID: {}", keycloakId);

                // Check if this user is already a manager in Keycloak
                boolean isManagerInKeycloak = keycloakService.userHasRole(keycloakId, "MANAGER");
                if (!isManagerInKeycloak) {
                    logger.info("Existing admin user in Keycloak is not a manager, assigning manager role");
                    boolean roleAssigned = keycloakService.updateKeycloakRole(keycloakId, "MANAGER");
                    if (!roleAssigned) {
                        logger.error("Failed to assign manager role to existing admin user in Keycloak");
                        failApplication();
                        return;
                    }
                    logger.info("Manager role assigned to existing admin user");
                }
            } else {
                // Create admin in Keycloak
                logger.info("Creating admin user in Keycloak...");

                keycloakId = keycloakService.createKeycloakUser(
                        adminUsername,
                        adminPassword,
                        "MANAGER",
                        adminFirstName,
                        adminLastName
                );

                if (keycloakId == null) {
                    logger.error("Failed to create admin user in Keycloak");
                    failApplication();
                    return;
                }

                newUserCreated = true;
                logger.info("Admin user created in Keycloak with ID: {}", keycloakId);
            }

            // At this point, we have a valid Keycloak user with ID and manager role

            // Create admin in local database
            try {
                User adminUser = new User();
                adminUser.setUsername(adminUsername);
                adminUser.setKeycloakId(keycloakId);
                adminUser.setRole("MANAGER");

                userRepository.save(adminUser);
                logger.info("Admin user created in local database");
            } catch (Exception e) {
                logger.error("Failed to create admin user in local database: {}", e.getMessage(), e);

                // If we created a new user in Keycloak, delete it to maintain consistency
                if (newUserCreated && keycloakId != null) {
                    logger.info("Rolling back Keycloak user creation");
                    boolean deleted = keycloakService.deleteKeycloakUser(keycloakId);
                    if (!deleted) {
                        logger.error("Failed to delete Keycloak user during rollback");
                    }
                }

                failApplication();
                return;
            }

            logger.info("Initial admin setup completed successfully");
        } catch (Exception e) {
            logger.error("Unexpected error during initial admin setup: {}", e.getMessage(), e);
            failApplication();
        }
    }

    /**
     * Terminates the application with an error exit code
     */
    private void failApplication() {
        logger.error("Initial admin setup failed - shutting down application");
        SpringApplication.exit(context, () -> 1);
    }
}