package ch.hoffmann.jan.warehouse.service;

import ch.hoffmann.jan.warehouse.dto.product.ProductCreateRequestDTO;
import ch.hoffmann.jan.warehouse.dto.product.ProductPatchRequestDTO;
import ch.hoffmann.jan.warehouse.dto.product.ProductResponseDTO;
import ch.hoffmann.jan.warehouse.dto.product.ProductStockDTO;
import ch.hoffmann.jan.warehouse.exception.WarehouseException;
import ch.hoffmann.jan.warehouse.model.Category;
import ch.hoffmann.jan.warehouse.model.Product;
import ch.hoffmann.jan.warehouse.model.Stock;
import ch.hoffmann.jan.warehouse.repository.CategoryRepository;
import ch.hoffmann.jan.warehouse.repository.ProductRepository;
import ch.hoffmann.jan.warehouse.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, StockRepository stockRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToResponseDTO)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Product", "id", id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Category", "id", categoryId));

        return productRepository.findByCategory(category).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponseDTO createProduct(ProductCreateRequestDTO createRequest) {
        // Check if product with the same name already exists
        if (productRepository.existsByName(createRequest.getName())) {
            throw new WarehouseException.DuplicateResourceException("Product", "name", createRequest.getName());
        }

        // Find the category
        Category category = categoryRepository.findById(createRequest.getCategoryId())
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Category", "id", createRequest.getCategoryId()));

        // Create and save the new product
        Product product = new Product();
        product.setName(createRequest.getName());
        product.setDescription(createRequest.getDescription());
        product.setPrice(createRequest.getPrice());
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);
        return convertToResponseDTO(savedProduct);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductCreateRequestDTO updateRequest) {
        // Find the product to update
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Product", "id", id));

        // Check if the name is actually changing
        if (!Objects.equals(product.getName(), updateRequest.getName())) {
            // Check if another product with the new name already exists
            if (productRepository.existsByName(updateRequest.getName())) {
                throw new WarehouseException.DuplicateResourceException("Product", "name", updateRequest.getName());
            }

            // Update the name
            product.setName(updateRequest.getName());
        }

        // Find the category
        Category category = categoryRepository.findById(updateRequest.getCategoryId())
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Category", "id", updateRequest.getCategoryId()));

        // Update the product
        product.setDescription(updateRequest.getDescription());
        product.setPrice(updateRequest.getPrice());
        product.setCategory(category);

        // Save and return the updated product
        Product updatedProduct = productRepository.save(product);
        return convertToResponseDTO(updatedProduct);
    }

    @Transactional
    public ProductResponseDTO patchProduct(Long id, ProductPatchRequestDTO patchRequest) {
        // Find the product to update
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Product", "id", id));

        // Update name if provided
        if (patchRequest.getName() != null) {
            // Check if the name is actually changing
            if (!Objects.equals(product.getName(), patchRequest.getName())) {
                // Check if another product with the new name already exists
                if (productRepository.existsByName(patchRequest.getName())) {
                    throw new WarehouseException.DuplicateResourceException("Product", "name", patchRequest.getName());
                }

                // Update the name
                product.setName(patchRequest.getName());
            }
        }

        // Update description if provided
        if (patchRequest.getDescription() != null) {
            product.setDescription(patchRequest.getDescription());
        }

        // Update price if provided
        if (patchRequest.getPrice() != null) {
            product.setPrice(patchRequest.getPrice());
        }

        // Update category if provided
        if (patchRequest.getCategoryId() != null) {
            Category category = categoryRepository.findById(patchRequest.getCategoryId())
                    .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Category", "id", patchRequest.getCategoryId()));
            product.setCategory(category);
        }

        // Save and return the updated product
        Product updatedProduct = productRepository.save(product);
        return convertToResponseDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        // Find the product to delete
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Product", "id", id));

        // Check if the product has associated stocks
        List<Stock> stocks = stockRepository.findByProduct(product);
        if (!stocks.isEmpty()) {
            throw new WarehouseException.ProductInUseException(product.getName(), stocks.size());
        }

        // Delete the product
        productRepository.delete(product);
    }

    /**
     * Converts a Product entity to a ProductResponseDTO
     */
    private ProductResponseDTO convertToResponseDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());

        // Convert stocks to specialized DTOs
        List<ProductStockDTO> stockDTOs = stockRepository.findByProduct(product).stream()
                .map(this::convertToProductStockDTO)
                .collect(Collectors.toList());
        dto.setStocks(stockDTOs);

        return dto;
    }

    /**
     * Converts a Stock entity to a ProductStockDTO
     */
    private ProductStockDTO convertToProductStockDTO(Stock stock) {
        ProductStockDTO dto = new ProductStockDTO();
        dto.setId(stock.getId());
        dto.setWarehouseId(stock.getWarehouse().getId());
        dto.setWarehouseName(stock.getWarehouse().getName());
        dto.setQuantity(stock.getQuantity());
        return dto;
    }
}