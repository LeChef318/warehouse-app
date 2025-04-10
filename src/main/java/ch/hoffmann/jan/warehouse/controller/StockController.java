package ch.hoffmann.jan.warehouse.controller;

import ch.hoffmann.jan.warehouse.dto.stock.StockCreateRequestDTO;
import ch.hoffmann.jan.warehouse.dto.stock.StockResponseDTO;
import ch.hoffmann.jan.warehouse.dto.stock.StockTransferRequestDTO;
import ch.hoffmann.jan.warehouse.dto.stock.StockUpdateRequestDTO;
import ch.hoffmann.jan.warehouse.service.StockService;
import ch.hoffmann.jan.warehouse.util.SecurityUtils;
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
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of stocks")
    public ResponseEntity<List<StockResponseDTO>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get stocks by product", description = "Returns a list of stocks by product ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stocks found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<List<StockResponseDTO>> getStocksByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(stockService.getStocksByProduct(productId));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Get stocks by warehouse", description = "Returns a list of stocks by warehouse ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stocks found"),
            @ApiResponse(responseCode = "404", description = "Warehouse not found")
    })
    public ResponseEntity<List<StockResponseDTO>> getStocksByWarehouse(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(stockService.getStocksByWarehouse(warehouseId));
    }

    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    @Operation(summary = "Get stock by product and warehouse", description = "Returns a stock by product ID and warehouse ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock found"),
            @ApiResponse(responseCode = "404", description = "Stock, product, or warehouse not found")
    })
    public ResponseEntity<StockResponseDTO> getStockByProductAndWarehouse(
            @PathVariable Long productId,
            @PathVariable Long warehouseId) {
        return ResponseEntity.ok(stockService.getStockByProductAndWarehouse(productId, warehouseId));
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Create stock", description = "Creates a new stock entry (Manager only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Stock successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Product or warehouse not found"),
            @ApiResponse(responseCode = "409", description = "Stock already exists")
    })
    public ResponseEntity<StockResponseDTO> createStock(@Valid @RequestBody StockCreateRequestDTO createRequest) {
        Long userId = securityUtils.getCurrentUserId();
        return new ResponseEntity<>(stockService.createStock(createRequest, userId), HttpStatus.CREATED);
    }

    @PutMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update stock", description = "Updates the stock quantity (Manager only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Product, warehouse, or stock not found"),
            @ApiResponse(responseCode = "409", description = "Not enough stock available")
    })
    public ResponseEntity<StockResponseDTO> updateStock(@Valid @RequestBody StockUpdateRequestDTO updateRequest) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(stockService.updateStock(updateRequest, userId));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Transfer stock", description = "Transfers stock from one warehouse to another (Manager only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock successfully transferred"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Product, warehouse, or stock not found"),
            @ApiResponse(responseCode = "409", description = "Not enough stock available or same warehouse transfer")
    })
    public ResponseEntity<Void> transferStock(@Valid @RequestBody StockTransferRequestDTO transferRequest) {
        Long userId = securityUtils.getCurrentUserId();
        stockService.transferStock(transferRequest, userId);
        return ResponseEntity.ok().build();
    }
}