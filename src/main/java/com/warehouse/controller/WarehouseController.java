package com.warehouse.controller;

import com.warehouse.dto.WarehouseDTO;
import com.warehouse.model.Warehouse;
import com.warehouse.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    public ResponseEntity<List<WarehouseDTO>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warehouse by ID", description = "Returns a warehouse by ID")
    public ResponseEntity<WarehouseDTO> getWarehouseById(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getWarehouseById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Create warehouse", description = "Creates a new warehouse (Manager only)")
    public ResponseEntity<WarehouseDTO> createWarehouse(@RequestBody Warehouse warehouse) {
        return new ResponseEntity<>(warehouseService.createWarehouse(warehouse), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update warehouse", description = "Updates an existing warehouse (Manager only)")
    public ResponseEntity<WarehouseDTO> updateWarehouse(@PathVariable Long id, @RequestBody Warehouse warehouse) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, warehouse));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Delete warehouse", description = "Deletes a warehouse (Manager only)")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }
}

