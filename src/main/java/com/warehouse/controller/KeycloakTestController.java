package com.warehouse.controller;

import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/keycloak-test")
public class KeycloakTestController {

    @Autowired(required = false)
    private Keycloak keycloak;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @GetMapping
    public ResponseEntity<Map<String, Object>> testKeycloakConnection() {
        Map<String, Object> response = new HashMap<>();

        response.put("serverUrl", serverUrl);
        response.put("realm", realm);

        try {
            if (keycloak != null) {
                // Try to get realm info to test connection
                keycloak.realm(realm).toRepresentation();
                response.put("status", "Connected");
                response.put("message", "Successfully connected to Keycloak");
            } else {
                response.put("status", "Not initialized");
                response.put("message", "Keycloak client is not initialized");
            }
        } catch (Exception e) {
            response.put("status", "Error");
            response.put("message", "Failed to connect to Keycloak: " + e.getMessage());
            response.put("error", e.toString());
            response.put("stackTrace", e.getStackTrace());
        }

        return ResponseEntity.ok(response);
    }
}

