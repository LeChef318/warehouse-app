package com.warehouse.service;

import com.warehouse.dto.ProductDTO;
import com.warehouse.dto.StockDTO;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.model.Category;
import com.warehouse.model.Product;
import com.warehouse.model.Stock;
import com.warehouse.repository.CategoryRepository;
import com.warehouse.repository.ProductRepository;
import com.warehouse.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        
        return productRepository.findByCategory(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO createProduct(ProductDTO productDTO) {
        if (productRepository.existsByName(productDTO.getName())) {
            throw new RuntimeException("Product name already exists");
        }
        
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", productDTO.getCategoryId()));
        
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setCategory(category);
        
        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", productDTO.getCategoryId()));
        
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setCategory(category);
        
        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        productRepository.delete(product);
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        
        List<StockDTO> stockDTOs = stockRepository.findByProduct(product).stream()
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

