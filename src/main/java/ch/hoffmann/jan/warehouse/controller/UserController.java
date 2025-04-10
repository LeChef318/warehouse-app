package ch.hoffmann.jan.warehouse.controller;

import ch.hoffmann.jan.warehouse.dto.user.UserCreateRequestDTO;
import ch.hoffmann.jan.warehouse.dto.user.UserPatchRequestDTO;
import ch.hoffmann.jan.warehouse.dto.user.UserResponseDTO;
import ch.hoffmann.jan.warehouse.exception.WarehouseException;
import ch.hoffmann.jan.warehouse.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "Endpoints for user management")
public class UserController {

    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get all users", description = "Returns a list of all users (Manager only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        logger.info("Request to get all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get user by ID", description = "Returns a user by ID (Manager only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        logger.info("Request to get user with ID: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved current user"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Request to get current user: {}", auth.getName());
        return ResponseEntity.ok(userService.getUserByUsername(auth.getName()));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with EMPLOYEE role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Username already exists"),
            @ApiResponse(responseCode = "502", description = "Keycloak service error"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserCreateRequestDTO userCreateRequestDTO) {
        logger.info("Request to register new user with username: {}", userCreateRequestDTO.getUsername());
        UserResponseDTO createdUser = userService.createUser(userCreateRequestDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update user", description = "Updates an existing user (Manager only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "502", description = "Keycloak service error"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserPatchRequestDTO userPatchRequestDTO) {
        logger.info("Request to update user with ID: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, userPatchRequestDTO));
    }

    @PatchMapping("/me")
    @Operation(summary = "Update current user", description = "Updates the currently authenticated user's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserResponseDTO> updateCurrentUser(
            @Valid @RequestBody UserPatchRequestDTO userPatchRequestDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Request to update current user: {}", auth.getName());
        return ResponseEntity.ok(userService.updateCurrentUser(auth.getName(), userPatchRequestDTO));
    }

    @PutMapping("/{id}/promote")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Promote user to manager", description = "Promotes an employee to manager role (Manager only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully promoted"),
            @ApiResponse(responseCode = "400", description = "Invalid role transition"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "502", description = "Keycloak service error"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserResponseDTO> promoteUser(@PathVariable Long id) {
        logger.info("Request to promote user with ID: {}", id);
        try {
            return ResponseEntity.ok(userService.promoteToManager(id));
        } catch (WarehouseException.InvalidRoleTransitionException ex) {
            logger.warn("Invalid role transition: {}", ex.getMessage());
            throw ex; // Re-throw to let GlobalExceptionHandler handle it
        }
    }

    @PutMapping("/{id}/demote")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Demote user to employee", description = "Demotes a manager to employee role (Manager only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully demoted"),
            @ApiResponse(responseCode = "400", description = "Invalid role transition"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "502", description = "Keycloak service error"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserResponseDTO> demoteUser(@PathVariable Long id) {
        logger.info("Request to demote user with ID: {}", id);
        return ResponseEntity.ok(userService.demoteToEmployee(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Delete user", description = "Deletes a user (Manager only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User successfully deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "502", description = "Keycloak service error"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("Request to delete user with ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}