package ch.hoffmann.jan.warehouse.dto.product;

public class ProductStockDTO {
    private Long id;
    private Long warehouseId;
    private String warehouseName;
    private int quantity;

    // Constructors
    public ProductStockDTO() {
    }

    public ProductStockDTO(Long id, Long warehouseId, String warehouseName, int quantity) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.quantity = quantity;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "ProductStockDTO{" +
                "id=" + id +
                ", warehouseId=" + warehouseId +
                ", warehouseName='" + warehouseName + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}