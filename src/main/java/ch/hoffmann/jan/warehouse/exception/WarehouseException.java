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

    /**
     * Exception thrown when a resource already exists.
     */
    public static class DuplicateResourceException extends WarehouseException {
        public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
            super(String.format("%s with %s '%s' already exists", resourceName, fieldName, fieldValue));
        }
    }

    /**
     * Exception thrown when a category is in use and cannot be deleted.
     */
    public static class CategoryInUseException extends WarehouseException {
        public CategoryInUseException(String categoryName, int productCount) {
            super(String.format("Cannot delete category '%s' because it contains %d product(s)",
                    categoryName, productCount));
        }
    }

    /**
     * Exception thrown when a warehouse has stock and cannot be deleted.
     */
    public static class WarehouseInUseException extends WarehouseException {
        public WarehouseInUseException(String warehouseName, int stockCount) {
            super(String.format("Cannot delete warehouse '%s' because it contains %d stock entries",
                    warehouseName, stockCount));
        }
    }

    /**
     * Exception thrown when a product has stock and cannot be deleted.
     */
    public static class ProductInUseException extends WarehouseException {
        public ProductInUseException(String productName, int stockCount) {
            super(String.format("Cannot delete product '%s' because it is used in %d stock entries",
                    productName, stockCount));
        }
    }

    /**
     * Exception thrown when there is not enough stock available for an operation.
     */
    public static class InsufficientStockException extends WarehouseException {
        public InsufficientStockException(String productName, String warehouseName, int requested, int available) {
            super(String.format("Not enough stock available for product '%s' in warehouse '%s'. Requested: %d, Available: %d",
                    productName, warehouseName, requested, available));
        }
    }

    /**
     * Exception thrown when trying to remove stock from a non-existent inventory.
     */
    public static class StockNotFoundException extends WarehouseException {
        public StockNotFoundException(String productName, String warehouseName) {
            super(String.format("No stock found for product '%s' in warehouse '%s'",
                    productName, warehouseName));
        }
    }

    /**
     * Exception thrown when trying to transfer stock to the same warehouse.
     */
    public static class SameWarehouseTransferException extends WarehouseException {
        public SameWarehouseTransferException(String warehouseName) {
            super(String.format("Cannot transfer stock to the same warehouse: '%s'",
                    warehouseName));
        }
    }

    /**
     * Exception thrown when an invalid audit action is provided.
     */
    public static class InvalidAuditActionException extends WarehouseException {
        public InvalidAuditActionException(String action) {
            super(String.format("Invalid audit action: '%s'. Valid actions are: ADD, REMOVE, TRANSFER", action));
        }
    }
}