package ch.hoffmann.jan.warehouse.dto.stock;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StockTransferRequestDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Source warehouse ID is required")
    private Long sourceWarehouseId;

    @NotNull(message = "Target warehouse ID is required")
    private Long targetWarehouseId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    // Constructors
    public StockTransferRequestDTO() {
    }

    public StockTransferRequestDTO(Long productId, Long sourceWarehouseId, Long targetWarehouseId, Integer quantity) {
        this.productId = productId;
        this.sourceWarehouseId = sourceWarehouseId;
        this.targetWarehouseId = targetWarehouseId;
        this.quantity = quantity;
    }

    // Getters and setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getSourceWarehouseId() {
        return sourceWarehouseId;
    }

    public void setSourceWarehouseId(Long sourceWarehouseId) {
        this.sourceWarehouseId = sourceWarehouseId;
    }

    public Long getTargetWarehouseId() {
        return targetWarehouseId;
    }

    public void setTargetWarehouseId(Long targetWarehouseId) {
        this.targetWarehouseId = targetWarehouseId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "StockTransferRequestDTO{" +
                "productId=" + productId +
                ", sourceWarehouseId=" + sourceWarehouseId +
                ", targetWarehouseId=" + targetWarehouseId +
                ", quantity=" + quantity +
                '}';
    }
}