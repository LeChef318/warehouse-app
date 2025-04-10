package ch.hoffmann.jan.warehouse.service;

import ch.hoffmann.jan.warehouse.dto.user.UserCreateRequestDTO;
import ch.hoffmann.jan.warehouse.dto.user.UserPatchRequestDTO;
import ch.hoffmann.jan.warehouse.dto.user.UserResponseDTO;
import ch.hoffmann.jan.warehouse.exception.WarehouseException;
import ch.hoffmann.jan.warehouse.model.User;
import ch.hoffmann.jan.warehouse.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final String ROLE_EMPLOYEE = "EMPLOYEE";
    private static final String ROLE_MANAGER = "MANAGER";

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserRepository userRepository, KeycloakService keycloakService) {
        this.userRepository = userRepository;
        this.keycloakService = keycloakService;
    }

    /**
     * Get all users
     */
    public List<UserResponseDTO> getAllUsers() {
        logger.debug("Getting all users");
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all active users
     */
    public List<UserResponseDTO> getAllActiveUsers() {
        logger.debug("Getting all active users");
        return userRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     */
    public UserResponseDTO getUserById(Long id) {
        logger.debug("Getting user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("User", "id", id));
        return convertToDTO(user);
    }

    /**
     * Get user by username
     */
    public UserResponseDTO getUserByUsername(String username) {
        logger.debug("Getting user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("User", "username", username));
        return convertToDTO(user);
    }

    /**
     * Create a new user
     */
    @Transactional
    public UserResponseDTO createUser(UserCreateRequestDTO userCreateRequestDTO) {
        logger.info("Creating new user with username: {}", userCreateRequestDTO.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(userCreateRequestDTO.getUsername())) {
            logger.warn("Username already exists: {}", userCreateRequestDTO.getUsername());
            throw new WarehouseException.UsernameAlreadyExistsException(userCreateRequestDTO.getUsername());
        }

        // Create user in Keycloak
        String keycloakId = null;
        try {
            keycloakId = keycloakService.createKeycloakUser(
                    userCreateRequestDTO.getUsername(),
                    userCreateRequestDTO.getPassword(),
                    ROLE_EMPLOYEE,
                    userCreateRequestDTO.getFirstname(),
                    userCreateRequestDTO.getLastname()
            );

            if (keycloakId == null) {
                logger.error("Failed to create user in Keycloak: {}", userCreateRequestDTO.getUsername());
                throw new WarehouseException.KeycloakOperationException("user creation", "Unknown error");
            }

            // Create user in local database
            User user = new User();
            user.setUsername(userCreateRequestDTO.getUsername());
            user.setRole(ROLE_EMPLOYEE);
            user.setFirstname(userCreateRequestDTO.getFirstname());
            user.setLastname(userCreateRequestDTO.getLastname());
            user.setKeycloakId(keycloakId);
            user.setActive(true); // Set as active by default

            User savedUser = userRepository.save(user);
            logger.info("User created successfully with ID: {}", savedUser.getId());
            return convertToDTO(savedUser);
        } catch (WarehouseException.UsernameAlreadyExistsException | WarehouseException.KeycloakOperationException e) {
            // These are already our custom exceptions, just rethrow them
            throw e;
        } catch (Exception e) {
            // Rollback Keycloak user creation if local database operation fails
            if (keycloakId != null) {
                try {
                    logger.warn("Rolling back Keycloak user creation due to local database failure");
                    boolean deleted = keycloakService.deleteKeycloakUser(keycloakId);
                    if (!deleted) {
                        logger.error("Failed to rollback Keycloak user creation: {}", keycloakId);
                    } else {
                        logger.info("Successfully rolled back Keycloak user creation: {}", keycloakId);
                    }
                } catch (Exception rollbackEx) {
                    logger.error("Error during Keycloak rollback: {}", rollbackEx.getMessage(), rollbackEx);
                }
            }

            // Wrap other exceptions
            logger.error("Error creating user: {}", e.getMessage(), e);
            throw new WarehouseException.KeycloakOperationException("user creation", e.getMessage());
        }
    }

    /**
     * Update user by ID (admin function)
     */
    @Transactional
    public UserResponseDTO updateUser(Long id, UserPatchRequestDTO userPatchRequestDTO) {
        logger.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("User", "id", id));

        // Check if user is active
        if (!user.isActive()) {
            logger.warn("Cannot update inactive user: {}", user.getUsername());
            throw new WarehouseException.UserInactiveException(user.getUsername());
        }

        // Check if username is being changed and if it already exists
        if (userPatchRequestDTO.getUsername() != null &&
                !userPatchRequestDTO.getUsername().equals(user.getUsername()) &&
                userRepository.existsByUsername(userPatchRequestDTO.getUsername())) {
            logger.warn("Cannot update user. Username already exists: {}", userPatchRequestDTO.getUsername());
            throw new WarehouseException.UsernameAlreadyExistsException(userPatchRequestDTO.getUsername());
        }

        // Update user in Keycloak
        try {
            boolean success = keycloakService.updateKeycloakUser(
                    user.getKeycloakId(),
                    userPatchRequestDTO.getUsername(),
                    userPatchRequestDTO.getPassword(),
                    userPatchRequestDTO.getFirstname(),
                    userPatchRequestDTO.getLastname()
            );

            if (!success) {
                logger.error("Failed to update user in Keycloak: {}", user.getUsername());
                throw new WarehouseException.KeycloakOperationException("user update", "Failed to update user in Keycloak");
            }

            // Update user in local database
            if (userPatchRequestDTO.getUsername() != null) {
                user.setUsername(userPatchRequestDTO.getUsername());
            }
            if (userPatchRequestDTO.getFirstname() != null) {
                user.setFirstname(userPatchRequestDTO.getFirstname());
            }
            if (userPatchRequestDTO.getLastname() != null) {
                user.setLastname(userPatchRequestDTO.getLastname());
            }

            User updatedUser = userRepository.save(user);
            logger.info("User updated successfully: {}", updatedUser.getId());
            return convertToDTO(updatedUser);
        } catch (WarehouseException e) {
            // Rethrow our custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage(), e);
            throw new WarehouseException.KeycloakOperationException("user update", e.getMessage());
        }
    }

    /**
     * Update current user (self-service function)
     */
    @Transactional
    public UserResponseDTO updateCurrentUser(String username, UserPatchRequestDTO userPatchRequestDTO) {
        logger.info("User {} updating their own information", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("User", "username", username));

        // Check if user is active
        if (!user.isActive()) {
            logger.warn("Inactive user attempted to update their information: {}", username);
            throw new WarehouseException.UserInactiveException(user.getUsername());
        }

        // Users can't change their own username
        if (userPatchRequestDTO.getUsername() != null &&
                !userPatchRequestDTO.getUsername().equals(user.getUsername())) {
            logger.warn("User {} attempted to change their username to {}", username, userPatchRequestDTO.getUsername());
            throw new WarehouseException.InsufficientPermissionException("You cannot change your username");
        }

        // Update user in Keycloak
        try {
            boolean success = keycloakService.updateKeycloakUser(
                    user.getKeycloakId(),
                    null, // Don't change username
                    userPatchRequestDTO.getPassword(),
                    userPatchRequestDTO.getFirstname(),
                    userPatchRequestDTO.getLastname()
            );

            if (!success) {
                logger.error("Failed to update user in Keycloak: {}", user.getUsername());
                throw new WarehouseException.KeycloakOperationException("user update", "Failed to update user in Keycloak");
            }

            // Update user in local database
            if (userPatchRequestDTO.getFirstname() != null) {
                user.setFirstname(userPatchRequestDTO.getFirstname());
            }
            if (userPatchRequestDTO.getLastname() != null) {
                user.setLastname(userPatchRequestDTO.getLastname());
            }

            User updatedUser = userRepository.save(user);
            logger.info("User updated their own information successfully: {}", updatedUser.getId());
            return convertToDTO(updatedUser);
        } catch (WarehouseException e) {
            // Rethrow our custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage(), e);
            throw new WarehouseException.KeycloakOperationException("user update", e.getMessage());
        }
    }

    /**
     * Promote user to manager
     */
    @Transactional
    public UserResponseDTO promoteToManager(Long id) {
        logger.info("Promoting user to manager. User ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("User", "id", id));

        // Check if user is active
        if (!user.isActive()) {
            logger.warn("Cannot promote inactive user: {}", user.getUsername());
            throw new WarehouseException.UserInactiveException(user.getUsername());
        }

        if (ROLE_MANAGER.equals(user.getRole())) {
            logger.warn("User is already a manager: {}", user.getUsername());
            throw new WarehouseException.InvalidRoleTransitionException(user.getUsername(), ROLE_MANAGER, ROLE_MANAGER);
        }

        // Update role in Keycloak
        try {
            boolean success = keycloakService.updateKeycloakRole(
                    user.getKeycloakId(),
                    ROLE_MANAGER
            );

            if (!success) {
                logger.error("Failed to update user role in Keycloak: {}", user.getUsername());
                throw new WarehouseException.KeycloakOperationException("role update", "Failed to update role in Keycloak");
            }

            user.setRole(ROLE_MANAGER);
            User updatedUser = userRepository.save(user);
            logger.info("User promoted to manager successfully: {}", updatedUser.getUsername());
            return convertToDTO(updatedUser);
        } catch (WarehouseException e) {
            // Rethrow our custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error promoting user: {}", e.getMessage(), e);
            throw new WarehouseException.KeycloakOperationException("role update", e.getMessage());
        }
    }

    /**
     * Demote user to employee
     */
    @Transactional
    public UserResponseDTO demoteToEmployee(Long id) {
        logger.info("Demoting user to employee. User ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("User", "id", id));

        // Check if user is active
        if (!user.isActive()) {
            logger.warn("Cannot demote inactive user: {}", user.getUsername());
            throw new WarehouseException.UserInactiveException(user.getUsername());
        }

        if (ROLE_EMPLOYEE.equals(user.getRole())) {
            logger.warn("User is already an employee: {}", user.getUsername());
            throw new WarehouseException.InvalidRoleTransitionException(user.getUsername(), ROLE_EMPLOYEE, ROLE_EMPLOYEE);
        }

        // Check if this is the last manager
        long managerCount = userRepository.countByRoleAndActiveTrue(ROLE_MANAGER);
        if (managerCount <= 1) {
            logger.warn("Cannot demote the last manager: {}", user.getUsername());
            throw new WarehouseException.ValidationException("Cannot demote the last manager");
        }

        // Update role in Keycloak
        try {
            boolean success = keycloakService.updateKeycloakRole(
                    user.getKeycloakId(),
                    ROLE_EMPLOYEE
            );

            if (!success) {
                logger.error("Failed to update user role in Keycloak: {}", user.getUsername());
                throw new WarehouseException.KeycloakOperationException("role update", "Failed to update role in Keycloak");
            }

            user.setRole(ROLE_EMPLOYEE);
            User updatedUser = userRepository.save(user);
            logger.info("User demoted to employee successfully: {}", updatedUser.getUsername());
            return convertToDTO(updatedUser);
        } catch (WarehouseException e) {
            // Rethrow our custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error demoting user: {}", e.getMessage(), e);
            throw new WarehouseException.KeycloakOperationException("role update", e.getMessage());
        }
    }

    /**
     * Deactivate user (soft delete)
     * Removes the user from Keycloak but keeps the record in the local database
     */
    @Transactional
    public void deactivateUser(Long id) {
        logger.info("Deactivating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("User", "id", id));

        // Check if user is already inactive
        if (!user.isActive()) {
            logger.warn("User is already inactive: {}", user.getUsername());
            throw new WarehouseException.ValidationException("User is already inactive");
        }

        // Check if this is the last manager
        if (ROLE_MANAGER.equals(user.getRole())) {
            long managerCount = userRepository.countByRoleAndActiveTrue(ROLE_MANAGER);
            if (managerCount <= 1) {
                logger.warn("Cannot deactivate the last manager: {}", user.getUsername());
                throw new WarehouseException.ValidationException("Cannot deactivate the last manager");
            }
        }

        // Delete user from Keycloak
        try {
            boolean deleted = keycloakService.deleteKeycloakUser(user.getKeycloakId());
            if (!deleted) {
                logger.error("Failed to delete user from Keycloak: {}", user.getKeycloakId());
                throw new WarehouseException.KeycloakOperationException("user deletion", "Failed to delete user from Keycloak");
            }

            // Mark user as inactive in local database
            user.setActive(false);
            userRepository.save(user);
            logger.info("User deactivated successfully: {}", user.getUsername());
        } catch (WarehouseException e) {
            // Rethrow our custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error deactivating user: {}", e.getMessage(), e);
            throw new WarehouseException.KeycloakOperationException("user deactivation", e.getMessage());
        }
    }

    /**
     * Delete user (for backward compatibility)
     * This now calls deactivateUser instead of actually deleting the record
     */
    @Transactional
    public void deleteUser(Long id) {
        logger.info("Delete user request redirected to deactivate user with ID: {}", id);
        deactivateUser(id);
    }

    /**
     * Convert User entity to UserResponseDTO
     */
    private UserResponseDTO convertToDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getFirstname(),
                user.getLastname()
        );
    }
}