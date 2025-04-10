package ch.hoffmann.jan.warehouse.service;

import ch.hoffmann.jan.warehouse.exception.WarehouseException;
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
     * @return The Keycloak user ID if successful
     * @throws WarehouseException.KeycloakOperationException if the operation fails
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
                String responseBody = "";
                try {
                    responseBody = response.readEntity(String.class);
                } catch (Exception e) {
                    logger.error("Could not read response body", e);
                }

                if (response.getStatus() == 409) {
                    throw new WarehouseException.UsernameAlreadyExistsException(username);
                } else {
                    throw new WarehouseException.KeycloakOperationException(
                            "user creation",
                            "HTTP Status: " + response.getStatus() + ", Response: " + responseBody
                    );
                }
            }

            try {
                userId = CreatedResponseUtil.getCreatedId(response);
                logger.debug("Created user ID: {}", userId);
            } catch (Exception e) {
                logger.error("Failed to get created ID", e);
                throw new WarehouseException.KeycloakOperationException("user creation", "Failed to get created user ID: " + e.getMessage());
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
                throw new WarehouseException.KeycloakOperationException("password setting", e.getMessage());
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
                throw new WarehouseException.KeycloakOperationException("role assignment", e.getMessage());
            }

            logger.info("Successfully created user in Keycloak: {}", username);
            return userId;
        } catch (WarehouseException e) {
            // Rethrow our custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error creating Keycloak user", e);
            throw new WarehouseException.KeycloakOperationException("user creation", e.getMessage());
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
     * @return true if successful
     * @throws WarehouseException.KeycloakOperationException if the operation fails
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
            throw new WarehouseException.KeycloakOperationException("user deletion", e.getMessage());
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
     * @return true if successful
     * @throws WarehouseException.KeycloakOperationException if the operation fails
     * @throws WarehouseException.UsernameAlreadyExistsException if the new username already exists
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

                    // Check if the exception message indicates a conflict (username already exists)
                    if (e.getMessage() != null && e.getMessage().contains("409")) {
                        throw new WarehouseException.UsernameAlreadyExistsException(newUsername);
                    } else {
                        throw new WarehouseException.KeycloakOperationException("user update", e.getMessage());
                    }
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
                    throw new WarehouseException.KeycloakOperationException("password update", e.getMessage());
                }
            }

            logger.info("Successfully updated user with ID: {}", userId);
            return true;
        } catch (WarehouseException e) {
            // Rethrow our custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error updating Keycloak user with ID: {}", userId, e);
            throw new WarehouseException.KeycloakOperationException("user update", e.getMessage());
        }
    }

    /**
     * Updates a user's role in Keycloak
     *
     * @param userId  The Keycloak user ID
     * @param newRole The new role to assign
     * @return true if successful
     * @throws WarehouseException.KeycloakOperationException if the operation fails
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
                    throw new WarehouseException.KeycloakOperationException("role update", e.getMessage());
                }
            }

            logger.info("No role update needed for user ID: {}", userId);
            return true;
        } catch (WarehouseException e) {
            // Rethrow our custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error updating role for Keycloak user with ID: {}", userId, e);
            throw new WarehouseException.KeycloakOperationException("role update", e.getMessage());
        }
    }

    /**
     * Checks if a user with the given username exists in Keycloak
     *
     * @param username The username to check
     * @return true if the user exists, false otherwise
     * @throws WarehouseException.KeycloakOperationException if the operation fails
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
            throw new WarehouseException.KeycloakOperationException("user existence check", e.getMessage());
        }
    }

    /**
     * Gets the Keycloak user ID for a given username
     *
     * @param username The username to look up
     * @return The Keycloak user ID
     * @throws WarehouseException.ResourceNotFoundException if the user is not found
     * @throws WarehouseException.KeycloakOperationException if the operation fails
     */
    public String getUserIdByUsername(String username) {
        logger.debug("Getting user ID for username: {}", username);
        try {
            RealmResource realmResource = keycloak.realm(realm);
            List<UserRepresentation> users = realmResource.users().search(username, true);
            if (users == null || users.isEmpty()) {
                logger.debug("No user found with username: {}", username);
                throw new WarehouseException.ResourceNotFoundException("User", "username", username);
            }
            String userId = users.getFirst().getId();
            logger.debug("Found user ID: {} for username: {}", userId, username);
            return userId;
        } catch (WarehouseException e) {
            // Rethrow our custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error getting user ID from Keycloak for username: {}", username, e);
            throw new WarehouseException.KeycloakOperationException("user ID lookup", e.getMessage());
        }
    }

    /**
     * Checks if a user has a specific role in Keycloak
     *
     * @param userId The Keycloak user ID
     * @param role   The role name to check
     * @return true if the user has the role, false otherwise
     * @throws WarehouseException.KeycloakOperationException if the operation fails
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
            throw new WarehouseException.KeycloakOperationException("role check", e.getMessage());
        }
    }

    /**
     * Checks if Keycloak is available and configured correctly
     *
     * @return true if Keycloak is available
     * @throws WarehouseException.KeycloakOperationException if Keycloak is not available
     */
    public boolean isKeycloakAvailable() {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            realmResource.toRepresentation();
            logger.info("Keycloak is available and configured correctly");
            return true;
        } catch (Exception e) {
            logger.error("Keycloak is not available: {}", e.getMessage(), e);
            throw new WarehouseException.KeycloakOperationException("availability check", e.getMessage());
        }
    }

    /**
     * Verifies that the required roles exist in Keycloak
     *
     * @return true if all required roles exist
     * @throws WarehouseException.KeycloakOperationException if the required roles don't exist
     */
    public boolean verifyRequiredRoles() {
        try {
            RealmResource realmResource = keycloak.realm(realm);

            // Check EMPLOYEE role
            try {
                realmResource.roles().get("EMPLOYEE").toRepresentation();
            } catch (Exception e) {
                logger.error("EMPLOYEE role does not exist in Keycloak", e);
                throw new WarehouseException.KeycloakOperationException("role verification", "EMPLOYEE role does not exist");
            }

            // Check MANAGER role
            try {
                realmResource.roles().get("MANAGER").toRepresentation();
            } catch (Exception e) {
                logger.error("MANAGER role does not exist in Keycloak", e);
                throw new WarehouseException.KeycloakOperationException("role verification", "MANAGER role does not exist");
            }

            logger.info("All required roles exist in Keycloak");
            return true;
        } catch (WarehouseException e) {
            // Rethrow our custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error verifying required roles in Keycloak", e);
            throw new WarehouseException.KeycloakOperationException("role verification", e.getMessage());
        }
    }

    /**
     * Gets all users from Keycloak
     *
     * @return List of all user representations
     * @throws WarehouseException.KeycloakOperationException if the operation fails
     */
    public List<UserRepresentation> getAllUsers() {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            List<UserRepresentation> users = realmResource.users().list();
            if (users == null) {
                throw new WarehouseException.KeycloakOperationException("user listing", "Received null response from Keycloak");
            }
            return users;
        } catch (WarehouseException e) {
            // Rethrow our custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error getting all users from Keycloak: {}", e.getMessage(), e);
            throw new WarehouseException.KeycloakOperationException("user listing", e.getMessage());
        }
    }

    /**
     * Gets the primary role of a user from Keycloak
     * Prioritizes MANAGER over EMPLOYEE
     *
     * @param userId The Keycloak user ID
     * @return The role name
     * @throws WarehouseException.KeycloakOperationException if the operation fails
     * @throws WarehouseException.ValidationException if no recognized role is found
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
            throw new WarehouseException.ValidationException("User has no recognized role (MANAGER or EMPLOYEE)");
        } catch (WarehouseException e) {
            // Rethrow our custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error getting role for user {}: {}", userId, e.getMessage(), e);
            throw new WarehouseException.KeycloakOperationException("role retrieval", e.getMessage());
        }
    }
}