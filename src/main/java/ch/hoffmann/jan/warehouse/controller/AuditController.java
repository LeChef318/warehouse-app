package ch.hoffmann.jan.warehouse.controller;

import ch.hoffmann.jan.warehouse.dto.audit.AuditFilterDTO;
import ch.hoffmann.jan.warehouse.dto.audit.AuditResponseDTO;
import ch.hoffmann.jan.warehouse.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    @Operation(summary = "Get audit logs with filtering and pagination", description = "Returns a paginated list of audit logs with optional filtering (Manager only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved audit logs"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Page<AuditResponseDTO>> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        AuditFilterDTO filterDTO = new AuditFilterDTO();
        filterDTO.setUserId(userId);
        filterDTO.setProductId(productId);
        filterDTO.setWarehouseId(warehouseId);
        filterDTO.setAction(action);
        filterDTO.setStartDate(startDate);
        filterDTO.setEndDate(endDate);
        filterDTO.setPage(page);
        filterDTO.setSize(size);

        return ResponseEntity.ok(auditService.getAuditLogs(filterDTO));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent audit logs", description = "Returns a list of the 10 most recent audit logs (Manager only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recent audit logs"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<AuditResponseDTO>> getRecentAuditLogs() {
        return ResponseEntity.ok(auditService.getRecentAuditLogs());
    }
}