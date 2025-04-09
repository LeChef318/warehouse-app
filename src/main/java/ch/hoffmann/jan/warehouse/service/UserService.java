package ch.hoffmann.jan.warehouse.service;

import ch.hoffmann.jan.warehouse.dto.user.UserPatchRequestDTO;
import ch.hoffmann.jan.warehouse.dto.user.UserResponseDTO;
import ch.hoffmann.jan.warehouse.dto.user.UserCreateRequestDTO;
import ch.hoffmann.jan.warehouse.exception.ResourceNotFoundException;
import ch.hoffmann.jan.warehouse.model.User;
import ch.hoffmann.jan.warehouse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;

    @Autowired
    public UserService(UserRepository userRepository, KeycloakService keycloakService) {
        this.userRepository = userRepository;
        this.keycloakService = keycloakService;
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    public UserResponseDTO createUser(UserCreateRequestDTO userCreateRequestDTO) {
        if (userRepository.existsByUsername(userCreateRequestDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Create user in Keycloak
        String keycloakUserCreated = keycloakService.createKeycloakUser(
                userCreateRequestDTO.getUsername(),
                userCreateRequestDTO.getPassword(),
                "EMPLOYEE",
                userCreateRequestDTO.getFirstname(),
                userCreateRequestDTO.getLastname()
        );

        if (keycloakUserCreated == null) {
            throw new RuntimeException("Failed to create user in Keycloak");
        }

        User user = new User();
        user.setUsername(userCreateRequestDTO.getUsername());
        user.setRole("EMPLOYEE");
        user.setFirstname(userCreateRequestDTO.getFirstname());
        user.setLastname(userCreateRequestDTO.getLastname());
        user.setKeycloakId(keycloakUserCreated);

        // Create user in database
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    public UserResponseDTO updateUser(Long id, UserPatchRequestDTO userPatchRequestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Update user in Keycloak
        String keycloakUserUpdated = keycloakService.updateKeycloakUser(
                user.getUsername(),
                userPatchRequestDTO.getUsername(),
                userPatchRequestDTO.getPassword(),
                userPatchRequestDTO.getFirstname(),
                userPatchRequestDTO.getLastname()
        );

        if (keycloakUserUpdated != null) {
            throw new RuntimeException("Failed to update user in Keycloak" + keycloakUserUpdated);
        }
        // Update user in database
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
        return convertToDTO(updatedUser);
    }

    public UserResponseDTO promoteToManager(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if ("MANAGER".equals(user.getRole())) {
            throw new RuntimeException("User is already a manager");
        }

        // Update role in Keycloak
        String keycloakRoleUpdated = keycloakService.updateKeycloakRole(
                user.getKeycloakId(),
                "MANAGER"
        );

        if (keycloakRoleUpdated != null) {
            throw new RuntimeException("Failed to update user role in Keycloak" + keycloakRoleUpdated);
        }

        user.setRole("MANAGER");
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public UserResponseDTO demoteToEmployee(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if ("EMPLOYEE".equals(user.getRole())) {
            throw new RuntimeException("User is already an employee");
        }

        // Update role in Keycloak
        String keycloakRoleUpdated = keycloakService.updateKeycloakRole(
                user.getKeycloakId(),
                "EMPLOYEE"
        );

        if (keycloakRoleUpdated != null) {
            throw new RuntimeException("Failed to update user role in Keycloak" + keycloakRoleUpdated);
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

