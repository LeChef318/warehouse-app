package ch.hoffmann.jan.warehouse.dto;

import java.time.LocalDateTime;

public class AuditDTO {
    private Long id;
    private String username;
    private String userRole;
    private String action;
    private String productName;
    private String warehouseName;
    private String targetWarehouseName;
    private Integer quantity;
    private LocalDateTime timestamp;

    public AuditDTO() {
    }

    public AuditDTO(Long id, String username, String userRole, String action, String productName, String warehouseName, String targetWarehouseName, Integer quantity, LocalDateTime timestamp) {
        this.id = id;
        this.username = username;
        this.userRole = userRole;
        this.action = action;
        this.productName = productName;
        this.warehouseName = warehouseName;
        this.targetWarehouseName = targetWarehouseName;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getTargetWarehouseName() {
        return targetWarehouseName;
    }

    public void setTargetWarehouseName(String targetWarehouseName) {
        this.targetWarehouseName = targetWarehouseName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

