package com.warehouse.controller;

import com.warehouse.dto.UserDTO;
import com.warehouse.model.User;
import com.warehouse.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "Endpoints for user management")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get all users", description = "Returns a list of all users (Manager only)")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get user by ID", description = "Returns a user by ID (Manager only)")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with EMPLOYEE role")
    public ResponseEntity<UserDTO> registerUser(@RequestBody User user) {
        // Force the role to be EMPLOYEE for public registration
        user.setRole("EMPLOYEE");
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update user", description = "Updates an existing user (Manager only)")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @PutMapping("/{id}/promote")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Promote user to manager", description = "Promotes an employee to manager role (Manager only)")
    public ResponseEntity<UserDTO> promoteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.promoteToManager(id));
    }

    @PutMapping("/{id}/demote")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Demote user to employee", description = "Demotes a manager to employee role (Manager only)")
    public ResponseEntity<UserDTO> demoteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.demoteToEmployee(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Delete user", description = "Deletes a user (Manager only)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

