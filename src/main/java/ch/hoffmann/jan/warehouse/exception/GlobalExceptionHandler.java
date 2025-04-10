package ch.hoffmann.jan.warehouse.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation exceptions from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Error");
        body.put("details", errors);
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle username already exists exception
     */
    @ExceptionHandler(WarehouseException.UsernameAlreadyExistsException.class)
    public ResponseEntity<Object> handleUsernameAlreadyExists(
            WarehouseException.UsernameAlreadyExistsException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Username Conflict");
        body.put("message", ex.getMessage());
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        logger.warn("Username conflict: {}", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Handle Keycloak operation exceptions
     */
    @ExceptionHandler(WarehouseException.KeycloakOperationException.class)
    public ResponseEntity<Object> handleKeycloakOperation(
            WarehouseException.KeycloakOperationException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_GATEWAY.value());
        body.put("error", "External Service Error");
        body.put("message", ex.getMessage());
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        logger.error("Keycloak operation failed: {}", ex.getMessage(), ex);

        return new ResponseEntity<>(body, HttpStatus.BAD_GATEWAY);
    }

    /**
     * Handle invalid role transition exceptions
     */
    @ExceptionHandler(WarehouseException.InvalidRoleTransitionException.class)
    public ResponseEntity<Object> handleInvalidRoleTransition(
            WarehouseException.InvalidRoleTransitionException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Invalid Operation");
        body.put("message", ex.getMessage());
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        logger.warn("Invalid role transition: {}", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle inactive user exceptions
     */
    @ExceptionHandler(WarehouseException.UserInactiveException.class)
    public ResponseEntity<Object> handleUserInactive(
            WarehouseException.UserInactiveException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "User Inactive");
        body.put("message", ex.getMessage());
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        logger.warn("Operation attempted on inactive user: {}", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle insufficient permission exceptions
     */
    @ExceptionHandler(WarehouseException.InsufficientPermissionException.class)
    public ResponseEntity<Object> handleInsufficientPermission(
            WarehouseException.InsufficientPermissionException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Access Denied");
        body.put("message", ex.getMessage());
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        logger.warn("Insufficient permissions: {}", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(WarehouseException.ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(
            WarehouseException.ResourceNotFoundException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Resource Not Found");
        body.put("message", ex.getMessage());
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        logger.info("Resource not found: {}", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(WarehouseException.ValidationException.class)
    public ResponseEntity<Object> handleValidation(
            WarehouseException.ValidationException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Error");
        body.put("message", ex.getMessage());
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        logger.info("Validation error: {}", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle synchronization exceptions
     */
    @ExceptionHandler(WarehouseException.SynchronizationException.class)
    public ResponseEntity<Object> handleSynchronization(
            WarehouseException.SynchronizationException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Synchronization Error");
        body.put("message", ex.getMessage());
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        logger.error("Synchronization error: {}", ex.getMessage(), ex);

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle duplicate resource exceptions
     */
    @ExceptionHandler(WarehouseException.DuplicateResourceException.class)
    public ResponseEntity<Object> handleDuplicateResourceException(
            WarehouseException.DuplicateResourceException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Resource Conflict");
        body.put("message", ex.getMessage());
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        logger.warn("Resource conflict: {}", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Handle category in use exceptions
     */
    @ExceptionHandler(WarehouseException.CategoryInUseException.class)
    public ResponseEntity<Object> handleCategoryInUseException(
            WarehouseException.CategoryInUseException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Category In Use");
        body.put("message", ex.getMessage());
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        logger.warn("Category in use: {}", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Catch-all handler for any other WarehouseException
     */
    @ExceptionHandler(WarehouseException.class)
    public ResponseEntity<Object> handleWarehouseException(
            WarehouseException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Application Error");
        body.put("message", ex.getMessage());
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        logger.error("Unhandled warehouse exception: {}", ex.getMessage(), ex);

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Catch-all handler for any other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(
            Exception ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Server Error");
        body.put("message", "An unexpected error occurred");
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        logger.error("Unhandled exception: {}", ex.getMessage(), ex);

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle warehouse in use exceptions
     */
    @ExceptionHandler(WarehouseException.WarehouseInUseException.class)
    public ResponseEntity<Object> handleWarehouseInUseException(
            WarehouseException.WarehouseInUseException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Warehouse In Use");
        body.put("message", ex.getMessage());
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        logger.warn("Warehouse in use: {}", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Handle product in use exceptions
     */
    @ExceptionHandler(WarehouseException.ProductInUseException.class)
    public ResponseEntity<Object> handleProductInUseException(
            WarehouseException.ProductInUseException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Product In Use");
        body.put("message", ex.getMessage());
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        logger.warn("Product in use: {}", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }
}