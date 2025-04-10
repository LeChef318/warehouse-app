package ch.hoffmann.jan.warehouse.controller;

import ch.hoffmann.jan.warehouse.dto.warehouse.WarehouseCreateRequestDTO;
import ch.hoffmann.jan.warehouse.dto.warehouse.WarehousePatchRequestDTO;
import ch.hoffmann.jan.warehouse.dto.warehouse.WarehouseResponseDTO;
import ch.hoffmann.jan.warehouse.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@Tag(name = "Warehouse Controller", description = "Endpoints for warehouse management")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @Autowired
    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    @Operation(summary = "Get all warehouses", description = "Returns a list of all warehouses")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of warehouses")
    public ResponseEntity<List<WarehouseResponseDTO>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warehouse by ID", description = "Returns a warehouse by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Warehouse found"),
            @ApiResponse(responseCode = "404", description = "Warehouse not found")
    })
    public ResponseEntity<WarehouseResponseDTO> getWarehouseById(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getWarehouseById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Create warehouse", description = "Creates a new warehouse (Manager only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Warehouse successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Warehouse with this name already exists")
    })
    public ResponseEntity<WarehouseResponseDTO> createWarehouse(@Valid @RequestBody WarehouseCreateRequestDTO createRequest) {
        return new ResponseEntity<>(warehouseService.createWarehouse(createRequest), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update warehouse completely", description = "Completely replaces an existing warehouse (Manager only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Warehouse successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Warehouse not found"),
            @ApiResponse(responseCode = "409", description = "Warehouse with this name already exists")
    })
    public ResponseEntity<WarehouseResponseDTO> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody WarehouseCreateRequestDTO updateRequest) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, updateRequest));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update warehouse partially", description = "Partially updates an existing warehouse (Manager only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Warehouse successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Warehouse not found"),
            @ApiResponse(responseCode = "409", description = "Warehouse with this name already exists")
    })
    public ResponseEntity<WarehouseResponseDTO> patchWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody WarehousePatchRequestDTO patchRequest) {
        return ResponseEntity.ok(warehouseService.patchWarehouse(id, patchRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Delete warehouse", description = "Deletes a warehouse (Manager only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Warehouse successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Warehouse not found"),
            @ApiResponse(responseCode = "409", description = "Warehouse cannot be deleted because it contains stock")
    })
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }
}