package com.warehouse.controller;

import com.warehouse.dto.StockDTO;
import com.warehouse.dto.StockUpdateDTO;
import com.warehouse.service.StockService;
import com.warehouse.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@Tag(name = "Stock Controller", description = "Endpoints for stock management")
public class StockController {

    private final StockService stockService;
    private final SecurityUtils securityUtils;

    @Autowired
    public StockController(StockService stockService, SecurityUtils securityUtils) {
        this.stockService = stockService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    @Operation(summary = "Get all stocks", description = "Returns a list of all stocks")
    public ResponseEntity<List<StockDTO>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get stocks by product", description = "Returns a list of stocks by product ID")
    public ResponseEntity<List<StockDTO>> getStocksByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(stockService.getStocksByProduct(productId));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Get stocks by warehouse", description = "Returns a list of stocks by warehouse ID")
    public ResponseEntity<List<StockDTO>> getStocksByWarehouse(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(stockService.getStocksByWarehouse(warehouseId));
    }

    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    @Operation(summary = "Get stock by product and warehouse", description = "Returns a stock by product ID and warehouse ID")
    public ResponseEntity<StockDTO> getStockByProductAndWarehouse(@PathVariable Long productId, @PathVariable Long warehouseId) {
        return ResponseEntity.ok(stockService.getStockByProductAndWarehouse(productId, warehouseId));
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update stock", description = "Updates the stock of a product in a warehouse (Manager only)")
    public ResponseEntity<StockDTO> updateStock(@RequestBody StockUpdateDTO stockUpdateDTO) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(stockService.updateStock(stockUpdateDTO, userId));
    }

    @PutMapping("/transfer")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Transfer stock", description = "Transfers stock from one warehouse to another (Manager only)")
    public ResponseEntity<Void> transferStock(@RequestBody StockUpdateDTO stockUpdateDTO) {
        Long userId = securityUtils.getCurrentUserId();
        stockService.transferStock(stockUpdateDTO, userId);
        return ResponseEntity.ok().build();
    }
}

