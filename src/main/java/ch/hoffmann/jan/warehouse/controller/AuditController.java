package ch.hoffmann.jan.warehouse.controller;

import ch.hoffmann.jan.warehouse.dto.audit.AuditResponseDTO;
import ch.hoffmann.jan.warehouse.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit Controller", description = "Endpoints for audit log management")
public class AuditController {

    private final AuditService auditService;

    @Autowired
    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get paginated audit logs", description = "Returns a paginated list of all audit logs ordered by timestamp (Manager only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved audit logs"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Page<AuditResponseDTO>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditService.getAuditLogs(page, size));
    }

    @GetMapping("/recent")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get recent audit logs", description = "Returns the 10 most recent audit logs (Manager only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recent audit logs"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<AuditResponseDTO>> getRecentAuditLogs() {
        return ResponseEntity.ok(auditService.getRecentAuditLogs());
    }
}