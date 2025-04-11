package ch.hoffmann.jan.warehouse.controller;

import ch.hoffmann.jan.warehouse.dto.product.ProductCreateRequestDTO;
import ch.hoffmann.jan.warehouse.dto.product.ProductPatchRequestDTO;
import ch.hoffmann.jan.warehouse.dto.product.ProductResponseDTO;
import ch.hoffmann.jan.warehouse.exception.WarehouseException;
import ch.hoffmann.jan.warehouse.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ProductResponseDTO productResponseDTO;
    private ProductCreateRequestDTO createRequestDTO;
    private ProductPatchRequestDTO patchRequestDTO;
    private List<ProductResponseDTO> productList;

    @BeforeEach
    void setUp() {
        // Set up test data
        productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setId(1L);
        productResponseDTO.setName("Test Product");
        productResponseDTO.setDescription("Test Description");
        productResponseDTO.setPrice(new BigDecimal("19.99"));
        productResponseDTO.setCategoryId(1L);
        productResponseDTO.setCategoryName("Test Category");

        createRequestDTO = new ProductCreateRequestDTO();
        createRequestDTO.setName("New Product");
        createRequestDTO.setDescription("New Description");
        createRequestDTO.setPrice(new BigDecimal("29.99"));
        createRequestDTO.setCategoryId(1L);

        patchRequestDTO = new ProductPatchRequestDTO();
        patchRequestDTO.setName("Updated Product");
        patchRequestDTO.setPrice(new BigDecimal("39.99"));

        ProductResponseDTO product2 = new ProductResponseDTO();
        product2.setId(2L);
        product2.setName("Another Product");
        product2.setPrice(new BigDecimal("9.99"));
        product2.setCategoryId(1L);
        product2.setCategoryName("Test Category");

        productList = Arrays.asList(productResponseDTO, product2);
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Arrange
        when(productService.getAllProducts()).thenReturn(productList);

        // Act
        ResponseEntity<List<ProductResponseDTO>> response = productController.getAllProducts();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("Test Product", response.getBody().get(0).getName());
        assertEquals("Another Product", response.getBody().get(1).getName());
        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void getProductById_WithValidId_ShouldReturnProduct() {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(productResponseDTO);

        // Act
        ResponseEntity<ProductResponseDTO> response = productController.getProductById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Product", response.getBody().getName());
        assertEquals(new BigDecimal("19.99"), response.getBody().getPrice());
        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    void getProductById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(productService.getProductById(999L)).thenThrow(
                new WarehouseException.ResourceNotFoundException("Product", "id", 999L));

        // Act & Assert
        assertThrows(WarehouseException.ResourceNotFoundException.class, () -> {
            productController.getProductById(999L);
        });
        verify(productService, times(1)).getProductById(999L);
    }

    @Test
    void getProductsByCategory_ShouldReturnProductsInCategory() {
        // Arrange
        when(productService.getProductsByCategory(1L)).thenReturn(productList);

        // Act
        ResponseEntity<List<ProductResponseDTO>> response = productController.getProductsByCategory(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(productService, times(1)).getProductsByCategory(1L);
    }

    @Test
    void createProduct_WithValidData_ShouldReturnCreatedProduct() {
        // Arrange
        when(productService.createProduct(any(ProductCreateRequestDTO.class))).thenReturn(productResponseDTO);

        // Act
        ResponseEntity<ProductResponseDTO> response = productController.createProduct(createRequestDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Test Product", response.getBody().getName());
        verify(productService, times(1)).createProduct(any(ProductCreateRequestDTO.class));
    }

    @Test
    void updateProduct_WithValidData_ShouldReturnUpdatedProduct() {
        // Arrange
        when(productService.updateProduct(eq(1L), any(ProductCreateRequestDTO.class))).thenReturn(productResponseDTO);

        // Act
        ResponseEntity<ProductResponseDTO> response = productController.updateProduct(1L, createRequestDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Product", response.getBody().getName());
        verify(productService, times(1)).updateProduct(eq(1L), any(ProductCreateRequestDTO.class));
    }

    @Test
    void patchProduct_WithValidData_ShouldReturnUpdatedProduct() {
        // Arrange
        when(productService.patchProduct(eq(1L), any(ProductPatchRequestDTO.class))).thenReturn(productResponseDTO);

        // Act
        ResponseEntity<ProductResponseDTO> response = productController.patchProduct(1L, patchRequestDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Product", response.getBody().getName());
        verify(productService, times(1)).patchProduct(eq(1L), any(ProductPatchRequestDTO.class));
    }

    @Test
    void deleteProduct_WithValidId_ShouldReturnNoContent() {
        // Arrange
        doNothing().when(productService).deleteProduct(1L);

        // Act
        ResponseEntity<Void> response = productController.deleteProduct(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    void deleteProduct_WithInvalidId_ShouldThrowException() {
        // Arrange
        doThrow(new WarehouseException.ResourceNotFoundException("Product", "id", 999L))
                .when(productService).deleteProduct(999L);

        // Act & Assert
        assertThrows(WarehouseException.ResourceNotFoundException.class, () -> {
            productController.deleteProduct(999L);
        });
        verify(productService, times(1)).deleteProduct(999L);
    }
}