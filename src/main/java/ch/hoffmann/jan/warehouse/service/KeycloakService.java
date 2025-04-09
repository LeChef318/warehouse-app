package ch.hoffmann.jan.warehouse.service;

import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

@Service
public class KeycloakService {

    private final Logger logger = LoggerFactory.getLogger(KeycloakService.class);

    @Autowired
    private Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    /**
     * Creates a new user in Keycloak with the specified attributes
     *
     * @param username  Username for the new user
     * @param password  Password for the new user
     * @param role      Role to assign to the user (MANAGER or EMPLOYEE)
     * @param firstName First name of the user (optional)
     * @param lastName  Last name of the user (optional)
     * @return The Keycloak user ID if successful, null if failed
     */
    public String createKeycloakUser(String username, String password, String role, String firstName, String lastName) {
        logger.info("Creating Keycloak user: {}", username);
        Response response = null;
        String userId = null;

        try {
            // Get realm
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Create user representation
            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEnabled(true);
            user.setEmailVerified(true);

            if (firstName != null && !firstName.isEmpty()) {
                user.setFirstName(firstName);
            }

            if (lastName != null && !lastName.isEmpty()) {
                user.setLastName(lastName);
            }

            // Create user
            response = usersResource.create(user);

            if (response.getStatus() < 200 || response.getStatus() >= 300) {
                logger.error("Failed to create user in Keycloak. Status: {}", response.getStatus());
                try {
                    String responseBody = response.readEntity(String.class);
                    logger.error("Response body: {}", responseBody);
                } catch (Exception e) {
                    logger.error("Could not read response body", e);
                }
                return null;
            }

            try {
                userId = CreatedResponseUtil.getCreatedId(response);
                logger.debug("Created user ID: {}", userId);
            } catch (Exception e) {
                logger.error("Failed to get created ID", e);
                return null;
            } finally {
                if (response != null) {
                    response.close();
                }
            }

            // Set password
            try {
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(password);
                credential.setTemporary(false);
                usersResource.get(userId).resetPassword(credential);
                logger.debug("Password set for user: {}", userId);
            } catch (Exception e) {
                logger.error("Failed to set password for user: {}", userId, e);
                // Rollback - delete the user we just created
                try {
                    usersResource.get(userId).remove();
                    logger.info("Rolled back user creation due to password failure");
                } catch (Exception ex) {
                    logger.error("Failed to rollback user creation", ex);
                }
                return null;
            }

            // Assign role
            try {
                String validRole = "MANAGER".equals(role) ? "MANAGER" : "EMPLOYEE";
                RoleRepresentation roleRepresentation = realmResource.roles().get(validRole).toRepresentation();
                usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(roleRepresentation));
                logger.debug("Role {} assigned to user: {}", validRole, userId);
            } catch (Exception e) {
                logger.error("Failed to assign role to user: {}", userId, e);
                // Rollback - delete the user we just created
                try {
                    usersResource.get(userId).remove();
                    logger.info("Rolled back user creation due to role assignment failure");
                } catch (Exception ex) {
                    logger.error("Failed to rollback user creation", ex);
                }
                return null;
            }

            logger.info("Successfully created user in Keycloak: {}", username);
            return userId;
        } catch (Exception e) {
            logger.error("Unexpected error creating Keycloak user", e);
            return null;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * Deletes a user from Keycloak
     *
     * @param userId The Keycloak user ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteKeycloakUser(String userId) {
        logger.info("Deleting Keycloak user with ID: {}", userId);
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            usersResource.get(userId).remove();
            logger.info("Successfully deleted user with ID: {}", userId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to delete Keycloak user with ID: {}", userId, e);
            return false;
        }
    }

    /**
     * Updates a user in Keycloak
     *
     * @param userId       The Keycloak user ID to update
     * @param newUsername  New username (optional)
     * @param newPassword  New password (optional)
     * @param newFirstname New first name (optional)
     * @param newLastname  New last name (optional)
     * @return null if successful, error message otherwise
     */
    public boolean updateKeycloakUser(String userId, String newUsername, String newPassword, String newFirstname, String newLastname) {
        logger.info("Updating Keycloak user with ID: {}", userId);
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Find the user
            UserRepresentation user = usersResource.get(userId).toRepresentation();

            boolean updated = false;

            // Handle username update if provided
            if (newUsername != null && !newUsername.isEmpty()) {
                user.setUsername(newUsername);
                updated = true;
            }

            // Handle first name update if provided
            if (newFirstname != null && !newFirstname.isEmpty()) {
                user.setFirstName(newFirstname);
                updated = true;
            }

            // Handle last name update if provided
            if (newLastname != null && !newLastname.isEmpty()) {
                user.setLastName(newLastname);
                updated = true;
            }

            // Update user representation if any field was changed
            if (updated) {
                try {
                    usersResource.get(userId).update(user);
                    logger.debug("Updated user details for ID: {}", userId);
                } catch (Exception e) {
                    logger.error("Failed to update user details for ID: {}", userId, e);
                    return false;
                }
            }

            // Handle password update if provided
            if (newPassword != null && !newPassword.isEmpty()) {
                try {
                    CredentialRepresentation credential = new CredentialRepresentation();
                    credential.setType(CredentialRepresentation.PASSWORD);
                    credential.setValue(newPassword);
                    credential.setTemporary(false);
                    usersResource.get(userId).resetPassword(credential);
                    logger.debug("Updated password for user ID: {}", userId);
                } catch (Exception e) {
                    logger.error("Failed to update password for user ID: {}", userId, e);
                    return false;
                }
            }

            logger.info("Successfully updated user with ID: {}", userId);
            return true;
        } catch (Exception e) {
            logger.error("Unexpected error updating Keycloak user with ID: {}", userId, e);
            return false;
        }
    }

    /**
     * Updates a user's role in Keycloak
     *
     * @param userId  The Keycloak user ID
     * @param newRole The new role to assign
     * @return true if successful, false otherwise
     */
    public boolean updateKeycloakRole(String userId, String newRole) {
        logger.info("Updating role for Keycloak user with ID: {} to {}", userId, newRole);
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Find the user
            UserRepresentation user = usersResource.get(userId).toRepresentation();

            // Handle role update if provided
            if (newRole != null && !newRole.isEmpty()) {
                try {
                    // Get current roles
                    List<RoleRepresentation> currentRoles = usersResource.get(userId).roles().realmLevel().listAll();

                    // Remove current roles
                    if (!currentRoles.isEmpty()) {
                        usersResource.get(userId).roles().realmLevel().remove(currentRoles);
                    }

                    // Add new role
                    RoleRepresentation roleRepresentation = realmResource.roles().get(newRole).toRepresentation();
                    usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(roleRepresentation));
                    logger.debug("Updated role for user ID: {} to {}", userId, newRole);
                    return true;
                } catch (Exception e) {
                    logger.error("Failed to update role for user ID: {}", userId, e);
                    return false;
                }
            }

            logger.info("No role update needed for user ID: {}", userId);
            return true;
        } catch (Exception e) {
            logger.error("Unexpected error updating role for Keycloak user with ID: {}", userId, e);
            return false;
        }
    }

    /**
     * Checks if a user with the given username exists in Keycloak
     *
     * @param username The username to check
     * @return true if the user exists, false otherwise
     */
    public boolean userExistsByUsername(String username) {
        logger.debug("Checking if user exists in Keycloak: {}", username);
        try {
            RealmResource realmResource = keycloak.realm(realm);
            List<UserRepresentation> users = realmResource.users().search(username, true);
            boolean exists = users != null && !users.isEmpty();
            logger.debug("User {} exists in Keycloak: {}", username, exists);
            return exists;
        } catch (Exception e) {
            logger.error("Error checking if user exists in Keycloak: {}", username, e);
            return false;
        }
    }

    /**
     * Gets the Keycloak user ID for a given username
     *
     * @param username The username to look up
     * @return The Keycloak user ID, or null if not found
     */
    public String getUserIdByUsername(String username) {
        logger.debug("Getting user ID for username: {}", username);
        try {
            RealmResource realmResource = keycloak.realm(realm);
            List<UserRepresentation> users = realmResource.users().search(username, true);
            if (users == null || users.isEmpty()) {
                logger.debug("No user found with username: {}", username);
                return null;
            }
            String userId = users.getFirst().getId();
            logger.debug("Found user ID: {} for username: {}", userId, username);
            return userId;
        } catch (Exception e) {
            logger.error("Error getting user ID from Keycloak for username: {}", username, e);
            return null;
        }
    }

    /**
     * Checks if a user has a specific role in Keycloak
     *
     * @param userId The Keycloak user ID
     * @param role   The role name to check
     * @return true if the user has the role, false otherwise
     */
    public boolean userHasRole(String userId, String role) {
        logger.debug("Checking if user {} has role: {}", userId, role);
        try {
            RealmResource realmResource = keycloak.realm(realm);
            List<RoleRepresentation> roles = realmResource.users().get(userId).roles().realmLevel().listAll();
            boolean hasRole = roles.stream().anyMatch(r -> r.getName().equals(role));
            logger.debug("User {} has role {}: {}", userId, role, hasRole);
            return hasRole;
        } catch (Exception e) {
            logger.error("Error checking if user has role in Keycloak: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Checks if Keycloak is available and configured correctly
     *
     * @return true if Keycloak is available, false otherwise
     */
    public boolean isKeycloakAvailable() {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            realmResource.toRepresentation();
            logger.info("Keycloak is available and configured correctly");
            return true;
        } catch (Exception e) {
            logger.error("Keycloak is not available: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verifies that the required roles exist in Keycloak
     *
     * @return true if all required roles exist, false otherwise
     */
    public boolean verifyRequiredRoles() {
        try {
            RealmResource realmResource = keycloak.realm(realm);

            // Check EMPLOYEE role
            try {
                realmResource.roles().get("EMPLOYEE").toRepresentation();
            } catch (Exception e) {
                logger.error("EMPLOYEE role does not exist in Keycloak", e);
                return false;
            }

            // Check MANAGER role
            try {
                realmResource.roles().get("MANAGER").toRepresentation();
            } catch (Exception e) {
                logger.error("MANAGER role does not exist in Keycloak", e);
                return false;
            }

            logger.info("All required roles exist in Keycloak");
            return true;
        } catch (Exception e) {
            logger.error("Error verifying required roles in Keycloak", e);
            return false;
        }
    }

    /**
     * Gets all users from Keycloak
     *
     * @return List of all user representations
     */
    public List<UserRepresentation> getAllUsers() {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            return realmResource.users().list();
        } catch (Exception e) {
            logger.error("Error getting all users from Keycloak: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets the primary role of a user from Keycloak
     * Prioritizes MANAGER over EMPLOYEE
     *
     * @param userId The Keycloak user ID
     * @return The role name, or null if no role found
     */
    public String getUserRole(String userId) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            List<RoleRepresentation> roles = realmResource.users().get(userId).roles().realmLevel().listAll();

            // Check for MANAGER role first (priority)
            if (roles.stream().anyMatch(r -> r.getName().equals("MANAGER"))) {
                return "MANAGER";
            }

            // Check for EMPLOYEE role
            if (roles.stream().anyMatch(r -> r.getName().equals("EMPLOYEE"))) {
                return "EMPLOYEE";
            }

            // No recognized role found
            return null;
        } catch (Exception e) {
            logger.error("Error getting role for user {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }
}