package ch.hoffmann.jan.warehouse.config;

import ch.hoffmann.jan.warehouse.model.User;
import ch.hoffmann.jan.warehouse.repository.UserRepository;
import ch.hoffmann.jan.warehouse.service.KeycloakService;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Component responsible for synchronizing Keycloak users with the local database.
 * This runs before the InitialAdminSetup to ensure all users are properly synchronized.
 */
@Component
@Order(2) // Run before InitialAdminSetup which has Order(3)
public class KeycloakUserSynchronizer implements ApplicationRunner {

    private final KeycloakService keycloakService;
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(KeycloakUserSynchronizer.class);

    public KeycloakUserSynchronizer(KeycloakService keycloakService, UserRepository userRepository) {
        this.keycloakService = keycloakService;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        logger.info("Starting Keycloak user synchronization...");

        try {
            // Verify Keycloak is available
            if (!keycloakService.isKeycloakAvailable()) {
                logger.error("Keycloak is not available - skipping user synchronization");
                return;
            }

            // Get all users from Keycloak
            List<UserRepresentation> keycloakUsers = keycloakService.getAllUsers();
            logger.info("Found {} users in Keycloak", keycloakUsers.size());

            // Get all users from local database
            List<User> localUsers = userRepository.findAll();
            logger.info("Found {} users in local database", localUsers.size());

            // Create maps for easier lookup
            Map<String, UserRepresentation> keycloakUserMap = keycloakUsers.stream()
                    .collect(Collectors.toMap(UserRepresentation::getId, user -> user));

            Map<String, User> localUserByKeycloakIdMap = localUsers.stream()
                    .filter(user -> user.getKeycloakId() != null)
                    .collect(Collectors.toMap(User::getKeycloakId, user -> user));

            // Set of all Keycloak IDs
            Set<String> keycloakIds = keycloakUserMap.keySet();

            // Synchronize Keycloak users to local database
            for (UserRepresentation keycloakUser : keycloakUsers) {
                String keycloakId = keycloakUser.getId();
                String username = keycloakUser.getUsername();

                if (localUserByKeycloakIdMap.containsKey(keycloakId)) {
                    // User exists in local DB - update if needed
                    User localUser = localUserByKeycloakIdMap.get(keycloakId);

                    // Update username if it changed in Keycloak
                    if (!localUser.getUsername().equals(username)) {
                        logger.info("Updating username for user {}: {} -> {}",
                                keycloakId, localUser.getUsername(), username);
                        localUser.setUsername(username);
                    }

                    // Ensure user is marked as active
                    if (!localUser.isActive()) {
                        logger.info("Activating user: {}", username);
                        localUser.setActive(true);
                    }

                    // Update role if needed
                    String keycloakRole = keycloakService.getUserRole(keycloakId);
                    if (keycloakRole != null && !localUser.getRole().equals(keycloakRole)) {
                        logger.info("Updating role for user {}: {} -> {}",
                                username, localUser.getRole(), keycloakRole);
                        localUser.setRole(keycloakRole);
                    }

                    userRepository.save(localUser);
                } else {
                    // User doesn't exist in local DB - create new user
                    logger.info("Creating new local user for Keycloak user: {}", username);

                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setKeycloakId(keycloakId);
                    newUser.setActive(true);

                    // Get role from Keycloak
                    String role = keycloakService.getUserRole(keycloakId);
                    newUser.setRole(role != null ? role : "EMPLOYEE"); // Default to EMPLOYEE if no role found

                    userRepository.save(newUser);
                }
            }

            // Mark local users as inactive if they don't exist in Keycloak
            for (User localUser : localUsers) {
                String localKeycloakId = localUser.getKeycloakId();

                // Skip users without a Keycloak ID (might be legacy users)
                if (localKeycloakId == null) {
                    continue;
                }

                if (!keycloakIds.contains(localKeycloakId) && localUser.isActive()) {
                    logger.info("Marking user as inactive (not found in Keycloak): {}", localUser.getUsername());
                    localUser.setActive(false);
                    userRepository.save(localUser);
                }
            }

            logger.info("Keycloak user synchronization completed successfully");
        } catch (Exception e) {
            logger.error("Error during Keycloak user synchronization: {}", e.getMessage(), e);
            // We don't fail the application here, just log the error and continue
        }
    }
}