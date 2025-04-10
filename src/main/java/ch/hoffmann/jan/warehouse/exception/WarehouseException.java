package ch.hoffmann.jan.warehouse.exception;

/**
 * Base exception class for all warehouse application exceptions.
 * Contains nested classes for specific exception types.
 */
public class WarehouseException extends RuntimeException {

    public WarehouseException(String message) {
        super(message);
    }

    public WarehouseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Exception thrown when attempting to create a user with a username that already exists.
     */
    public static class UsernameAlreadyExistsException extends WarehouseException {
        public UsernameAlreadyExistsException(String username) {
            super("Username already exists: " + username);
        }
    }

    /**
     * Exception thrown when a Keycloak operation fails.
     */
    public static class KeycloakOperationException extends WarehouseException {
        public KeycloakOperationException(String operation, String reason) {
            super("Keycloak " + operation + " failed: " + reason);
        }

        public KeycloakOperationException(String operation, Throwable cause) {
            super("Keycloak " + operation + " failed: " + cause.getMessage(), cause);
        }
    }

    /**
     * Exception thrown when attempting an invalid role transition.
     */
    public static class InvalidRoleTransitionException extends WarehouseException {
        public InvalidRoleTransitionException(String username, String currentRole, String targetRole) {
            super("Cannot change role for user '" + username + "' from " + currentRole + " to " + targetRole);
        }
    }

    /**
     * Exception thrown when attempting to perform operations on an inactive user.
     */
    public static class UserInactiveException extends WarehouseException {
        public UserInactiveException(String username) {
            super("User is inactive: " + username);
        }
    }

    /**
     * Exception thrown when a user doesn't have sufficient permissions for an operation.
     */
    public static class InsufficientPermissionException extends WarehouseException {
        public InsufficientPermissionException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown when a requested resource is not found.
     */
    public static class ResourceNotFoundException extends WarehouseException {
        public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
            super(resourceName + " not found with " + fieldName + " : " + fieldValue);
        }
    }

    /**
     * Exception thrown when there's an issue with data validation.
     */
    public static class ValidationException extends WarehouseException {
        public ValidationException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown when there's an issue with synchronization between systems.
     */
    public static class SynchronizationException extends WarehouseException {
        public SynchronizationException(String message) {
            super(message);
        }

        public SynchronizationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}