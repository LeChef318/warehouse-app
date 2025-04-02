package com.warehouse.service;

import com.warehouse.dto.StockDTO;
import com.warehouse.dto.WarehouseDTO;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.model.Stock;
import com.warehouse.model.Warehouse;
import com.warehouse.repository.StockRepository;
import com.warehouse.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final StockRepository stockRepository;

    @Autowired
    public WarehouseService(WarehouseRepository warehouseRepository, StockRepository stockRepository) {
        this.warehouseRepository = warehouseRepository;
        this.stockRepository = stockRepository;
    }

    public List<WarehouseDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public WarehouseDTO getWarehouseById(Long id) {
        return warehouseRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));
    }

    public WarehouseDTO createWarehouse(Warehouse warehouse) {
        if (warehouseRepository.existsByName(warehouse.getName())) {
            throw new RuntimeException("Warehouse name already exists");
        }
        
        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        return convertToDTO(savedWarehouse);
    }

    public WarehouseDTO updateWarehouse(Long id, Warehouse warehouseDetails) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));
        
        warehouse.setName(warehouseDetails.getName());
        warehouse.setLocation(warehouseDetails.getLocation());
        
        Warehouse updatedWarehouse = warehouseRepository.save(warehouse);
        return convertToDTO(updatedWarehouse);
    }

    public void deleteWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));
        
        List<Stock> stocks = stockRepository.findByWarehouse(warehouse);
        if (!stocks.isEmpty()) {
            throw new RuntimeException("Cannot delete warehouse with existing stock");
        }
        
        warehouseRepository.delete(warehouse);
    }

    private WarehouseDTO convertToDTO(Warehouse warehouse) {
        WarehouseDTO dto = new WarehouseDTO();
        dto.setId(warehouse.getId());
        dto.setName(warehouse.getName());
        dto.setLocation(warehouse.getLocation());
        
        List<StockDTO> stockDTOs = stockRepository.findByWarehouse(warehouse).stream()
                .map(this::convertStockToDTO)
                .collect(Collectors.toList());
        dto.setStocks(stockDTOs);
        
        return dto;
    }

    private StockDTO convertStockToDTO(Stock stock) {
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

