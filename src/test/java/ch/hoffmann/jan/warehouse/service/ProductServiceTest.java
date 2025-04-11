package ch.hoffmann.jan.warehouse.service;

import ch.hoffmann.jan.warehouse.dto.product.ProductCreateRequestDTO;
import ch.hoffmann.jan.warehouse.dto.product.ProductPatchRequestDTO;
import ch.hoffmann.jan.warehouse.dto.product.ProductResponseDTO;
import ch.hoffmann.jan.warehouse.exception.WarehouseException;
import ch.hoffmann.jan.warehouse.model.Category;
import ch.hoffmann.jan.warehouse.model.Product;
import ch.hoffmann.jan.warehouse.model.Stock;
import ch.hoffmann.jan.warehouse.repository.CategoryRepository;
import ch.hoffmann.jan.warehouse.repository.ProductRepository;
import ch.hoffmann.jan.warehouse.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;
    private ProductCreateRequestDTO createRequestDTO;
    private ProductPatchRequestDTO patchRequestDTO;
    private List<Product> productList;
    private List<Stock> stockList;

    @BeforeEach
    void setUp() {
        // Set up test data
        category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("19.99"));
        product.setCategory(category);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Another Product");
        product2.setPrice(new BigDecimal("9.99"));
        product2.setCategory(category);

        productList = Arrays.asList(product, product2);

        createRequestDTO = new ProductCreateRequestDTO();
        createRequestDTO.setName("New Product");
        createRequestDTO.setDescription("New Description");
        createRequestDTO.setPrice(new BigDecimal("29.99"));
        createRequestDTO.setCategoryId(1L);

        patchRequestDTO = new ProductPatchRequestDTO();
        patchRequestDTO.setName("Updated Product");
        patchRequestDTO.setPrice(new BigDecimal("39.99"));

        stockList = new ArrayList<>(); // Empty stock list for tests
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Arrange
        when(productRepository.findAll()).thenReturn(productList);
        when(stockRepository.findByProduct(any(Product.class))).thenReturn(stockList);

        // Act
        List<ProductResponseDTO> result = productService.getAllProducts();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Test Product", result.get(0).getName());
        assertEquals("Another Product", result.get(1).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductById_WithValidId_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(stockRepository.findByProduct(product)).thenReturn(stockList);

        // Act
        ProductResponseDTO result = productService.getProductById(1L);

        // Assert
        assertEquals("Test Product", result.getName());
        assertEquals(new BigDecimal("19.99"), result.getPrice());
        assertEquals("Test Category", result.getCategoryName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(WarehouseException.ResourceNotFoundException.class, () -> {
            productService.getProductById(999L);
        });
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void getProductsByCategory_ShouldReturnProductsInCategory() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findByCategory(category)).thenReturn(productList);
        when(stockRepository.findByProduct(any(Product.class))).thenReturn(stockList);

        // Act
        List<ProductResponseDTO> result = productService.getProductsByCategory(1L);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Test Product", result.get(0).getName());
        assertEquals("Another Product", result.get(1).getName());
        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findByCategory(category);
    }

    @Test
    void createProduct_WithValidData_ShouldReturnCreatedProduct() {
        // Arrange
        when(productRepository.existsByName("New Product")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // Create a product that matches what would be saved
        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setName("New Product");
        savedProduct.setDescription("New Description");
        savedProduct.setPrice(new BigDecimal("29.99"));
        savedProduct.setCategory(category);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(stockRepository.findByProduct(any(Product.class))).thenReturn(stockList);

        // Act
        ProductResponseDTO result = productService.createProduct(createRequestDTO);

        // Assert
        assertEquals("New Product", result.getName());
        assertEquals(new BigDecimal("29.99"), result.getPrice());
        verify(productRepository, times(1)).existsByName("New Product");
        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_WithDuplicateName_ShouldThrowException() {
        // Arrange
        when(productRepository.existsByName("New Product")).thenReturn(true);

        // Act & Assert
        assertThrows(WarehouseException.DuplicateResourceException.class, () -> {
            productService.createProduct(createRequestDTO);
        });
        verify(productRepository, times(1)).existsByName("New Product");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_WithValidData_ShouldReturnUpdatedProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // Create a product that matches what would be returned after update
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("New Product");
        updatedProduct.setDescription("New Description");
        updatedProduct.setPrice(new BigDecimal("29.99"));
        updatedProduct.setCategory(category);

        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(stockRepository.findByProduct(any(Product.class))).thenReturn(stockList);

        // Act
        ProductResponseDTO result = productService.updateProduct(1L, createRequestDTO);

        // Assert
        assertEquals("New Product", result.getName());
        assertEquals(new BigDecimal("29.99"), result.getPrice());
        verify(productRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void patchProduct_WithValidData_ShouldReturnUpdatedProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Create a product that matches what would be returned after patch
        Product patchedProduct = new Product();
        patchedProduct.setId(1L);
        patchedProduct.setName("Updated Product");
        patchedProduct.setDescription("Test Description"); // Unchanged
        patchedProduct.setPrice(new BigDecimal("39.99"));
        patchedProduct.setCategory(category); // Unchanged

        when(productRepository.save(any(Product.class))).thenReturn(patchedProduct);
        when(stockRepository.findByProduct(any(Product.class))).thenReturn(stockList);

        // Act
        ProductResponseDTO result = productService.patchProduct(1L, patchRequestDTO);

        // Assert
        assertEquals("Updated Product", result.getName());
        assertEquals(new BigDecimal("39.99"), result.getPrice());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void deleteProduct_WithValidId_AndNoStock_ShouldDeleteProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(stockRepository.findByProduct(product)).thenReturn(new ArrayList<>());

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository, times(1)).findById(1L);
        verify(stockRepository, times(1)).findByProduct(product);
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    void deleteProduct_WithValidId_ButHasStock_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Stock stock = new Stock();
        stock.setId(1L);
        stock.setProduct(product);
        stock.setQuantity(10);

        List<Stock> stocksWithItems = Arrays.asList(stock);
        when(stockRepository.findByProduct(product)).thenReturn(stocksWithItems);

        // Act & Assert
        assertThrows(WarehouseException.ProductInUseException.class, () -> {
            productService.deleteProduct(1L);
        });
        verify(productRepository, times(1)).findById(1L);
        verify(stockRepository, times(1)).findByProduct(product);
        verify(productRepository, never()).delete(any(Product.class));
    }
}