package ch.hoffmann.jan.warehouse.dto.stock;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StockUpdateRequestDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Operation type is required")
    private OperationType operation;

    // Enum for operation type
    public enum OperationType {
        ADD,
        REMOVE
    }

    // Constructors
    public StockUpdateRequestDTO() {
    }

    public StockUpdateRequestDTO(Long productId, Long warehouseId, Integer quantity, OperationType operation) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.operation = operation;
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

    public OperationType getOperation() {
        return operation;
    }

    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public boolean isAddition() {
        return operation == OperationType.ADD;
    }

    @Override
    public String toString() {
        return "StockUpdateRequestDTO{" +
                "productId=" + productId +
                ", warehouseId=" + warehouseId +
                ", quantity=" + quantity +
                ", operation=" + operation +
                '}';
    }
}