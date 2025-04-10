package ch.hoffmann.jan.warehouse.service;

import ch.hoffmann.jan.warehouse.dto.warehouse.WarehouseCreateRequestDTO;
import ch.hoffmann.jan.warehouse.dto.warehouse.WarehousePatchRequestDTO;
import ch.hoffmann.jan.warehouse.dto.warehouse.WarehouseResponseDTO;
import ch.hoffmann.jan.warehouse.dto.warehouse.WarehouseStockDTO;
import ch.hoffmann.jan.warehouse.exception.WarehouseException;
import ch.hoffmann.jan.warehouse.model.Stock;
import ch.hoffmann.jan.warehouse.model.Warehouse;
import ch.hoffmann.jan.warehouse.repository.StockRepository;
import ch.hoffmann.jan.warehouse.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
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

    @Transactional(readOnly = true)
    public List<WarehouseResponseDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WarehouseResponseDTO getWarehouseById(Long id) {
        return warehouseRepository.findById(id)
                .map(this::convertToResponseDTO)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Warehouse", "id", id));
    }

    @Transactional
    public WarehouseResponseDTO createWarehouse(WarehouseCreateRequestDTO createRequest) {
        // Check if warehouse with the same name already exists
        if (warehouseRepository.existsByName(createRequest.getName())) {
            throw new WarehouseException.DuplicateResourceException("Warehouse", "name", createRequest.getName());
        }

        // Create and save the new warehouse
        Warehouse warehouse = new Warehouse();
        warehouse.setName(createRequest.getName());
        warehouse.setLocation(createRequest.getLocation());

        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        return convertToResponseDTO(savedWarehouse);
    }

    @Transactional
    public WarehouseResponseDTO updateWarehouse(Long id, WarehouseCreateRequestDTO updateRequest) {
        // Find the warehouse to update
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Warehouse", "id", id));

        // Check if the name is actually changing
        if (!Objects.equals(warehouse.getName(), updateRequest.getName())) {
            // Check if another warehouse with the new name already exists
            if (warehouseRepository.existsByName(updateRequest.getName())) {
                throw new WarehouseException.DuplicateResourceException("Warehouse", "name", updateRequest.getName());
            }

            // Update the name
            warehouse.setName(updateRequest.getName());
        }

        // Update the location
        warehouse.setLocation(updateRequest.getLocation());

        // Save and return the updated warehouse
        Warehouse updatedWarehouse = warehouseRepository.save(warehouse);
        return convertToResponseDTO(updatedWarehouse);
    }

    @Transactional
    public WarehouseResponseDTO patchWarehouse(Long id, WarehousePatchRequestDTO patchRequest) {
        // Find the warehouse to update
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Warehouse", "id", id));

        // Update name if provided
        if (patchRequest.getName() != null) {
            // Check if the name is actually changing
            if (!Objects.equals(warehouse.getName(), patchRequest.getName())) {
                // Check if another warehouse with the new name already exists
                if (warehouseRepository.existsByName(patchRequest.getName())) {
                    throw new WarehouseException.DuplicateResourceException("Warehouse", "name", patchRequest.getName());
                }

                // Update the name
                warehouse.setName(patchRequest.getName());
            }
        }

        // Update location if provided
        if (patchRequest.getLocation() != null) {
            warehouse.setLocation(patchRequest.getLocation());
        }

        // Save and return the updated warehouse
        Warehouse updatedWarehouse = warehouseRepository.save(warehouse);
        return convertToResponseDTO(updatedWarehouse);
    }

    @Transactional
    public void deleteWarehouse(Long id) {
        // Find the warehouse to delete
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Warehouse", "id", id));

        // Check if the warehouse has associated stocks
        List<Stock> stocks = stockRepository.findByWarehouse(warehouse);
        if (!stocks.isEmpty()) {
            throw new WarehouseException.WarehouseInUseException(warehouse.getName(), stocks.size());
        }

        // Delete the warehouse
        warehouseRepository.delete(warehouse);
    }

    /**
     * Converts a Warehouse entity to a WarehouseResponseDTO
     */
    private WarehouseResponseDTO convertToResponseDTO(Warehouse warehouse) {
        WarehouseResponseDTO dto = new WarehouseResponseDTO();
        dto.setId(warehouse.getId());
        dto.setName(warehouse.getName());
        dto.setLocation(warehouse.getLocation());

        // Convert stocks to specialized DTOs
        List<WarehouseStockDTO> stockDTOs = stockRepository.findByWarehouse(warehouse).stream()
                .map(this::convertToWarehouseStockDTO)
                .collect(Collectors.toList());
        dto.setStocks(stockDTOs);

        return dto;
    }

    /**
     * Converts a Stock entity to a WarehouseStockDTO
     */
    private WarehouseStockDTO convertToWarehouseStockDTO(Stock stock) {
        WarehouseStockDTO dto = new WarehouseStockDTO();
        dto.setId(stock.getId());
        dto.setProductId(stock.getProduct().getId());
        dto.setProductName(stock.getProduct().getName());
        dto.setQuantity(stock.getQuantity());
        return dto;
    }
}