package com.warehouse.controller;

import com.warehouse.dto.AuditDTO;
import com.warehouse.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('MANAGER')")
@Tag(name = "Audit Controller", description = "Endpoints for audit log management")
public class AuditController {

    private final AuditService auditService;

    @Autowired
    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @Operation(summary = "Get all audit logs", description = "Returns a list of all audit logs (Manager only)")
    public ResponseEntity<List<AuditDTO>> getAllAuditLogs() {
        return ResponseEntity.ok(auditService.getAllAuditLogs());
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent audit logs", description = "Returns a list of the 10 most recent audit logs (Manager only)")
    public ResponseEntity<List<AuditDTO>> getRecentAuditLogs() {
        return ResponseEntity.ok(auditService.getRecentAuditLogs());
    }
}

