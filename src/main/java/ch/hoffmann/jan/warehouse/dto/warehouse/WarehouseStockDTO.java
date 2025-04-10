package ch.hoffmann.jan.warehouse.dto.warehouse;

/**
 * Simplified DTO for stock information within warehouse responses
 */
public class WarehouseStockDTO {
    private Long id;
    private Long productId;
    private String productName;
    private int quantity;

    // Constructors
    public WarehouseStockDTO() {
    }

    public WarehouseStockDTO(Long id, Long productId, String productName, int quantity) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "WarehouseStockDTO{" +
                "id=" + id +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}