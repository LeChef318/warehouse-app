package com.warehouse.service;

import com.warehouse.dto.AuditDTO;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.model.Audit;
import com.warehouse.model.Product;
import com.warehouse.model.User;
import com.warehouse.model.Warehouse;
import com.warehouse.repository.AuditRepository;
import com.warehouse.repository.ProductRepository;
import com.warehouse.repository.UserRepository;
import com.warehouse.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private final AuditRepository auditRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    @Autowired
    public AuditService(AuditRepository auditRepository, UserRepository userRepository, 
                        ProductRepository productRepository, WarehouseRepository warehouseRepository) {
        this.auditRepository = auditRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public List<AuditDTO> getAllAuditLogs() {
        return auditRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AuditDTO> getRecentAuditLogs() {
        return auditRepository.findTop10ByOrderByTimestampDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AuditDTO createAuditLog(Long userId, Long productId, Long warehouseId, Long targetWarehouseId, String action, Integer quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));
        
        Warehouse targetWarehouse = null;
        if (targetWarehouseId != null) {
            targetWarehouse = warehouseRepository.findById(targetWarehouseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Target Warehouse", "id", targetWarehouseId));
        }
        
        Audit audit = new Audit();
        audit.setUser(user);
        audit.setProduct(product);
        audit.setWarehouse(warehouse);
        audit.setTargetWarehouse(targetWarehouse);
        audit.setAction(action);
        audit.setQuantity(quantity);
        audit.setTimestamp(LocalDateTime.now());
        
        Audit savedAudit = auditRepository.save(audit);
        return convertToDTO(savedAudit);
    }

    private AuditDTO convertToDTO(Audit audit) {
        AuditDTO dto = new AuditDTO();
        dto.setId(audit.getId());
        dto.setUsername(audit.getUser().getUsername());
        dto.setUserRole(audit.getUser().getRole());
        dto.setAction(audit.getAction());
        dto.setProductName(audit.getProduct().getName());
        dto.setWarehouseName(audit.getWarehouse().getName());
        dto.setTargetWarehouseName(audit.getTargetWarehouse() != null ? audit.getTargetWarehouse().getName() : null);
        dto.setQuantity(audit.getQuantity());
        dto.setTimestamp(audit.getTimestamp());
        return dto;
    }
}

