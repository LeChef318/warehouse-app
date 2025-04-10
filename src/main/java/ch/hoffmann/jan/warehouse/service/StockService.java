package ch.hoffmann.jan.warehouse.service;

import ch.hoffmann.jan.warehouse.dto.stock.StockCreateRequestDTO;
import ch.hoffmann.jan.warehouse.dto.stock.StockResponseDTO;
import ch.hoffmann.jan.warehouse.dto.stock.StockTransferRequestDTO;
import ch.hoffmann.jan.warehouse.dto.stock.StockUpdateRequestDTO;
import ch.hoffmann.jan.warehouse.exception.WarehouseException;
import ch.hoffmann.jan.warehouse.model.Product;
import ch.hoffmann.jan.warehouse.model.Stock;
import ch.hoffmann.jan.warehouse.model.Warehouse;
import ch.hoffmann.jan.warehouse.repository.ProductRepository;
import ch.hoffmann.jan.warehouse.repository.StockRepository;
import ch.hoffmann.jan.warehouse.repository.WarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final AuditService auditService;
    private final Logger logger = LoggerFactory.getLogger(StockService.class);

    @Autowired
    public StockService(StockRepository stockRepository, ProductRepository productRepository,
                        WarehouseRepository warehouseRepository, AuditService auditService) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<StockResponseDTO> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StockResponseDTO> getStocksByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Product", "id", productId));

        return stockRepository.findByProduct(product).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StockResponseDTO> getStocksByWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Warehouse", "id", warehouseId));

        return stockRepository.findByWarehouse(warehouse).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StockResponseDTO getStockByProductAndWarehouse(Long productId, Long warehouseId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Product", "id", productId));

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Warehouse", "id", warehouseId));

        Stock stock = stockRepository.findByProductAndWarehouse(product, warehouse)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Stock", "product and warehouse",
                        productId + ", " + warehouseId));

        return convertToResponseDTO(stock);
    }

    @Transactional
    public StockResponseDTO createStock(StockCreateRequestDTO createRequest, Long userId) {
        Product product = productRepository.findById(createRequest.getProductId())
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Product", "id", createRequest.getProductId()));

        Warehouse warehouse = warehouseRepository.findById(createRequest.getWarehouseId())
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Warehouse", "id", createRequest.getWarehouseId()));

        // Check if stock already exists
        Optional<Stock> existingStock = stockRepository.findByProductAndWarehouse(product, warehouse);
        if (existingStock.isPresent()) {
            throw new WarehouseException.DuplicateResourceException("Stock", "product and warehouse",
                    product.getName() + " in " + warehouse.getName());
        }

        // Create new stock
        Stock stock = new Stock();
        stock.setProduct(product);
        stock.setWarehouse(warehouse);
        stock.setQuantity(createRequest.getQuantity());

        Stock savedStock = stockRepository.save(stock);

        // Create audit log
        auditService.createAuditLog(
                userId,
                product.getId(),
                warehouse.getId(),
                null,
                "ADD",
                createRequest.getQuantity()
        );

        logger.info("Created new stock: {} units of product {} in warehouse {}",
                createRequest.getQuantity(), product.getName(), warehouse.getName());

        return convertToResponseDTO(savedStock);
    }

    @Transactional
    public StockResponseDTO updateStock(StockUpdateRequestDTO updateRequest, Long userId) {
        Product product = productRepository.findById(updateRequest.getProductId())
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Product", "id", updateRequest.getProductId()));

        Warehouse warehouse = warehouseRepository.findById(updateRequest.getWarehouseId())
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Warehouse", "id", updateRequest.getWarehouseId()));

        Optional<Stock> stockOptional = stockRepository.findByProductAndWarehouse(product, warehouse);
        Stock stock;

        if (stockOptional.isPresent()) {
            stock = stockOptional.get();
            if (updateRequest.isAddition()) {
                // Add stock
                stock.setQuantity(stock.getQuantity() + updateRequest.getQuantity());
                logger.info("Added {} units to stock of product {} in warehouse {}",
                        updateRequest.getQuantity(), product.getName(), warehouse.getName());
            } else {
                // Remove stock
                if (stock.getQuantity() < updateRequest.getQuantity()) {
                    throw new WarehouseException.InsufficientStockException(
                            product.getName(), warehouse.getName(), updateRequest.getQuantity(), stock.getQuantity());
                }
                stock.setQuantity(stock.getQuantity() - updateRequest.getQuantity());
                logger.info("Removed {} units from stock of product {} in warehouse {}",
                        updateRequest.getQuantity(), product.getName(), warehouse.getName());
            }
        } else {
            // If stock doesn't exist and we're trying to remove, throw exception
            if (!updateRequest.isAddition()) {
                throw new WarehouseException.StockNotFoundException(product.getName(), warehouse.getName());
            }

            // Create new stock if we're adding
            stock = new Stock();
            stock.setProduct(product);
            stock.setWarehouse(warehouse);
            stock.setQuantity(updateRequest.getQuantity());
            logger.info("Created new stock with {} units of product {} in warehouse {}",
                    updateRequest.getQuantity(), product.getName(), warehouse.getName());
        }

        Stock updatedStock = stockRepository.save(stock);

        // Create audit log
        auditService.createAuditLog(
                userId,
                product.getId(),
                warehouse.getId(),
                null,
                updateRequest.isAddition() ? "ADD" : "REMOVE",
                updateRequest.getQuantity()
        );

        return convertToResponseDTO(updatedStock);
    }

    @Transactional
    public void transferStock(StockTransferRequestDTO transferRequest, Long userId) {
        // Validate warehouses are different
        if (Objects.equals(transferRequest.getSourceWarehouseId(), transferRequest.getTargetWarehouseId())) {
            throw new WarehouseException.SameWarehouseTransferException(transferRequest.getSourceWarehouseId().toString());
        }

        Product product = productRepository.findById(transferRequest.getProductId())
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Product", "id", transferRequest.getProductId()));

        Warehouse sourceWarehouse = warehouseRepository.findById(transferRequest.getSourceWarehouseId())
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Source Warehouse", "id", transferRequest.getSourceWarehouseId()));

        Warehouse targetWarehouse = warehouseRepository.findById(transferRequest.getTargetWarehouseId())
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Target Warehouse", "id", transferRequest.getTargetWarehouseId()));

        // Check if source stock exists
        Stock sourceStock = stockRepository.findByProductAndWarehouse(product, sourceWarehouse)
                .orElseThrow(() -> new WarehouseException.StockNotFoundException(product.getName(), sourceWarehouse.getName()));

        // Check if there's enough stock
        if (sourceStock.getQuantity() < transferRequest.getQuantity()) {
            throw new WarehouseException.InsufficientStockException(
                    product.getName(), sourceWarehouse.getName(), transferRequest.getQuantity(), sourceStock.getQuantity());
        }

        // Reduce source stock
        sourceStock.setQuantity(sourceStock.getQuantity() - transferRequest.getQuantity());
        stockRepository.save(sourceStock);

        // Increase target stock
        Optional<Stock> targetStockOptional = stockRepository.findByProductAndWarehouse(product, targetWarehouse);
        Stock targetStock;

        if (targetStockOptional.isPresent()) {
            targetStock = targetStockOptional.get();
            targetStock.setQuantity(targetStock.getQuantity() + transferRequest.getQuantity());
        } else {
            targetStock = new Stock();
            targetStock.setProduct(product);
            targetStock.setWarehouse(targetWarehouse);
            targetStock.setQuantity(transferRequest.getQuantity());
        }

        stockRepository.save(targetStock);

        // Create audit log
        auditService.createAuditLog(
                userId,
                product.getId(),
                sourceWarehouse.getId(),
                targetWarehouse.getId(),
                "TRANSFER",
                transferRequest.getQuantity()
        );

        logger.info("Transferred {} units of product {} from warehouse {} to warehouse {}",
                transferRequest.getQuantity(), product.getName(), sourceWarehouse.getName(), targetWarehouse.getName());
    }

    /**
     * Converts a Stock entity to a StockResponseDTO
     */
    private StockResponseDTO convertToResponseDTO(Stock stock) {
        StockResponseDTO dto = new StockResponseDTO();
        dto.setId(stock.getId());
        dto.setProductId(stock.getProduct().getId());
        dto.setProductName(stock.getProduct().getName());
        dto.setWarehouseId(stock.getWarehouse().getId());
        dto.setWarehouseName(stock.getWarehouse().getName());
        dto.setQuantity(stock.getQuantity());
        return dto;
    }
}