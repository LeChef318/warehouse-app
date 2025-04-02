package com.warehouse.service;

import com.warehouse.dto.UserDTO;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.model.User;
import com.warehouse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakService keycloakService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, KeycloakService keycloakService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.keycloakService = keycloakService;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    public UserDTO createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Ensure role is valid (EMPLOYEE or MANAGER)
        if (!"EMPLOYEE".equals(user.getRole()) && !"MANAGER".equals(user.getRole())) {
            user.setRole("EMPLOYEE"); // Default to EMPLOYEE if invalid role
        }

        // Create user in Keycloak
        boolean keycloakUserCreated = keycloakService.createKeycloakUser(
                user.getUsername(),
                user.getPassword(),
                user.getRole()
        );

        if (!keycloakUserCreated) {
            throw new RuntimeException("Failed to create user in Keycloak");
        }

        // Create user in database
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    public UserDTO updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Update user in Keycloak
        boolean keycloakUserUpdated = keycloakService.updateKeycloakUser(
                user.getUsername(),
                userDetails.getUsername(),
                userDetails.getPassword(),
                userDetails.getRole()
        );

        if (!keycloakUserUpdated) {
            throw new RuntimeException("Failed to update user in Keycloak");
        }

        user.setUsername(userDetails.getUsername());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        user.setRole(userDetails.getRole());

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public UserDTO promoteToManager(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if ("MANAGER".equals(user.getRole())) {
            throw new RuntimeException("User is already a manager");
        }

        // Update role in Keycloak
        boolean keycloakRoleUpdated = keycloakService.updateKeycloakUser(
                user.getUsername(),
                user.getUsername(),
                null, // Don't change password
                "MANAGER"
        );

        if (!keycloakRoleUpdated) {
            throw new RuntimeException("Failed to update user role in Keycloak");
        }

        user.setRole("MANAGER");
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public UserDTO demoteToEmployee(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if ("EMPLOYEE".equals(user.getRole())) {
            throw new RuntimeException("User is already an employee");
        }

        // Update role in Keycloak
        boolean keycloakRoleUpdated = keycloakService.updateKeycloakUser(
                user.getUsername(),
                user.getUsername(),
                null, // Don't change password
                "EMPLOYEE"
        );

        if (!keycloakRoleUpdated) {
            throw new RuntimeException("Failed to update user role in Keycloak");
        }

        user.setRole("EMPLOYEE");
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Delete user from Keycloak
        boolean keycloakUserDeleted = keycloakService.deleteKeycloakUser(user.getUsername());

        if (!keycloakUserDeleted) {
            throw new RuntimeException("Failed to delete user from Keycloak");
        }

        userRepository.delete(user);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        return dto;
    }
}

