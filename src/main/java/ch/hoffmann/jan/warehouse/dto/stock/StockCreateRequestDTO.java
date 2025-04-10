package ch.hoffmann.jan.warehouse.dto.stock;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StockCreateRequestDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    // Constructors
    public StockCreateRequestDTO() {
    }

    public StockCreateRequestDTO(Long productId, Long warehouseId, Integer quantity) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
    }

    // Getters and setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "StockCreateRequestDTO{" +
                "productId=" + productId +
                ", warehouseId=" + warehouseId +
                ", quantity=" + quantity +
                '}';
    }
}