package com.warehouse.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

@Service
public class KeycloakService {

    @Autowired
    private Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public boolean createKeycloakUser(String username, String password, String role) {
        try {
            // Get realm
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Create user representation
            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEnabled(true);
            user.setEmailVerified(true);

            // Create user
            Response response = usersResource.create(user);
            String userId = extractCreatedId(response);

            if (userId == null) {
                return false;
            }

            // Set password
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);
            usersResource.get(userId).resetPassword(credential);

            // Assign role - ensure it's a valid role
            String validRole = "MANAGER".equals(role) ? "MANAGER" : "EMPLOYEE";
            RoleRepresentation roleRepresentation = realmResource.roles().get(validRole).toRepresentation();
            usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(roleRepresentation));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String extractCreatedId(Response response) {
        if (response.getStatus() != 201) {
            return null;
        }

        String location = response.getHeaderString("Location");
        if (location == null) {
            return null;
        }

        String[] parts = location.split("/");
        return parts[parts.length - 1];
    }

    public boolean deleteKeycloakUser(String username) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            List<UserRepresentation> users = usersResource.search(username, true);
            if (users.isEmpty()) {
                return false;
            }

            String userId = users.get(0).getId();
            usersResource.delete(userId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateKeycloakUser(String username, String newUsername, String newPassword, String newRole) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            List<UserRepresentation> users = usersResource.search(username, true);
            if (users.isEmpty()) {
                return false;
            }

            String userId = users.get(0).getId();
            UserRepresentation user = usersResource.get(userId).toRepresentation();

            // Update username if provided
            if (newUsername != null && !newUsername.isEmpty()) {
                user.setUsername(newUsername);
                usersResource.get(userId).update(user);
            }

            // Update password if provided
            if (newPassword != null && !newPassword.isEmpty()) {
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(newPassword);
                credential.setTemporary(false);
                usersResource.get(userId).resetPassword(credential);
            }

            // Update role if provided
            if (newRole != null && !newRole.isEmpty()) {
                // Ensure it's a valid role
                String validRole = "MANAGER".equals(newRole) ? "MANAGER" : "EMPLOYEE";

                // Remove existing roles
                List<RoleRepresentation> currentRoles = usersResource.get(userId).roles().realmLevel().listAll();
                usersResource.get(userId).roles().realmLevel().remove(currentRoles);

                // Add new role
                RoleRepresentation roleRepresentation = realmResource.roles().get(validRole).toRepresentation();
                usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(roleRepresentation));
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

