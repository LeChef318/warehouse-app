package ch.hoffmann.jan.warehouse.dto;

public class StockUpdateDTO {
    private Long productId;
    private Long warehouseId;
    private Long targetWarehouseId; // Only used for transfers
    private Integer quantity;
    private boolean isAddition;

    public StockUpdateDTO() {
    }

    public StockUpdateDTO(Long productId, Long warehouseId, Long targetWarehouseId, Integer quantity, boolean isAddition) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.targetWarehouseId = targetWarehouseId;
        this.quantity = quantity;
        this.isAddition = isAddition;
    }

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

    public boolean isAddition() {
        return isAddition;
    }

    public void setAddition(boolean addition) {
        isAddition = addition;
    }
}

