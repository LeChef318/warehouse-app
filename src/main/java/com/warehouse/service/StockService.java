package com.warehouse.service;

import com.warehouse.dto.StockDTO;
import com.warehouse.dto.StockUpdateDTO;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.model.Product;
import com.warehouse.model.Stock;
import com.warehouse.model.Warehouse;
import com.warehouse.repository.ProductRepository;
import com.warehouse.repository.StockRepository;
import com.warehouse.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final AuditService auditService;

    @Autowired
    public StockService(StockRepository stockRepository, ProductRepository productRepository, 
                        WarehouseRepository warehouseRepository, AuditService auditService) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.auditService = auditService;
    }

    public List<StockDTO> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<StockDTO> getStocksByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        return stockRepository.findByProduct(product).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<StockDTO> getStocksByWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));
        
        return stockRepository.findByWarehouse(warehouse).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public StockDTO getStockByProductAndWarehouse(Long productId, Long warehouseId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));
        
        Stock stock = stockRepository.findByProductAndWarehouse(product, warehouse)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", "product and warehouse", productId + ", " + warehouseId));
        
        return convertToDTO(stock);
    }

    @Transactional
    public StockDTO updateStock(StockUpdateDTO stockUpdateDTO, Long userId) {
        Product product = productRepository.findById(stockUpdateDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", stockUpdateDTO.getProductId()));
        
        Warehouse warehouse = warehouseRepository.findById(stockUpdateDTO.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", stockUpdateDTO.getWarehouseId()));
        
        Optional<Stock> stockOptional = stockRepository.findByProductAndWarehouse(product, warehouse);
        Stock stock;
        
        if (stockOptional.isPresent()) {
            stock = stockOptional.get();
            if (stockUpdateDTO.isAddition()) {
                stock.setQuantity(stock.getQuantity() + stockUpdateDTO.getQuantity());
            } else {
                if (stock.getQuantity() < stockUpdateDTO.getQuantity()) {
                    throw new RuntimeException("Not enough stock available");
                }
                stock.setQuantity(stock.getQuantity() - stockUpdateDTO.getQuantity());
            }
        } else {
            if (!stockUpdateDTO.isAddition()) {
                throw new RuntimeException("Cannot remove stock from a non-existent inventory");
            }
            stock = new Stock();
            stock.setProduct(product);
            stock.setWarehouse(warehouse);
            stock.setQuantity(stockUpdateDTO.getQuantity());
        }
        
        Stock updatedStock = stockRepository.save(stock);
        
        // Create audit log
        auditService.createAuditLog(
            userId,
            product.getId(),
            warehouse.getId(),
            null,
            stockUpdateDTO.isAddition() ? "ADD" : "REMOVE",
            stockUpdateDTO.getQuantity()
        );
        
        return convertToDTO(updatedStock);
    }

    @Transactional
    public void transferStock(StockUpdateDTO stockUpdateDTO, Long userId) {
        if (stockUpdateDTO.getTargetWarehouseId() == null) {
            throw new RuntimeException("Target warehouse is required for transfer");
        }
        
        Product product = productRepository.findById(stockUpdateDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", stockUpdateDTO.getProductId()));
        
        Warehouse sourceWarehouse = warehouseRepository.findById(stockUpdateDTO.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Source Warehouse", "id", stockUpdateDTO.getWarehouseId()));
        
        Warehouse targetWarehouse = warehouseRepository.findById(stockUpdateDTO.getTargetWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Target Warehouse", "id", stockUpdateDTO.getTargetWarehouseId()));
        
        Stock sourceStock = stockRepository.findByProductAndWarehouse(product, sourceWarehouse)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", "product and warehouse", product.getId() + ", " + sourceWarehouse.getId()));
        
        if (sourceStock.getQuantity() < stockUpdateDTO.getQuantity()) {
            throw new RuntimeException("Not enough stock available for transfer");
        }
        
        // Reduce source stock
        sourceStock.setQuantity(sourceStock.getQuantity() - stockUpdateDTO.getQuantity());
        stockRepository.save(sourceStock);
        
        // Increase target stock
        Optional<Stock> targetStockOptional = stockRepository.findByProductAndWarehouse(product, targetWarehouse);
        Stock targetStock;
        
        if (targetStockOptional.isPresent()) {
            targetStock = targetStockOptional.get();
            targetStock.setQuantity(targetStock.getQuantity() + stockUpdateDTO.getQuantity());
        } else {
            targetStock = new Stock();
            targetStock.setProduct(product);
            targetStock.setWarehouse(targetWarehouse);
            targetStock.setQuantity(stockUpdateDTO.getQuantity());
        }
        
        stockRepository.save(targetStock);
        
        // Create audit log
        auditService.createAuditLog(
            userId,
            product.getId(),
            sourceWarehouse.getId(),
            targetWarehouse.getId(),
            "TRANSFER",
            stockUpdateDTO.getQuantity()
        );
    }

    private StockDTO convertToDTO(Stock stock) {
        StockDTO dto = new StockDTO();
        dto.setId(stock.getId());
        dto.setProductId(stock.getProduct().getId());
        dto.setProductName(stock.getProduct().getName());
        dto.setWarehouseId(stock.getWarehouse().getId());
        dto.setWarehouseName(stock.getWarehouse().getName());
        dto.setQuantity(stock.getQuantity());
        return dto;
    }
}

