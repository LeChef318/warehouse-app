package ch.hoffmann.jan.warehouse.service;

import ch.hoffmann.jan.warehouse.dto.audit.AuditResponseDTO;
import ch.hoffmann.jan.warehouse.model.Audit;
import ch.hoffmann.jan.warehouse.model.Product;
import ch.hoffmann.jan.warehouse.model.User;
import ch.hoffmann.jan.warehouse.model.Warehouse;
import ch.hoffmann.jan.warehouse.repository.AuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private final AuditRepository auditRepository;

    @Autowired
    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    /**
     * Creates and saves an audit log entry
     */
    @Transactional
    public void logAuditEvent(User user, Product product, Warehouse warehouse,
                              Warehouse targetWarehouse, String action, Integer quantity) {
        Audit audit = new Audit();
        audit.setUser(user);
        audit.setProduct(product);
        audit.setWarehouse(warehouse);
        audit.setTargetWarehouse(targetWarehouse);
        audit.setAction(action);
        audit.setQuantity(quantity);
        audit.setTimestamp(LocalDateTime.now());

        auditRepository.save(audit);
    }

    /**
     * Saves a pre-constructed Audit object
     */
    @Transactional
    public void saveAudit(Audit audit) {
        auditRepository.save(audit);
    }

    // Simplified method to get paginated audit logs
    @Transactional(readOnly = true)
    public Page<AuditResponseDTO> getAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditRepository.findAllByOrderByTimestampDesc(pageable)
                .map(this::convertToResponseDTO);
    }

    // Method for recent logs
    @Transactional(readOnly = true)
    public List<AuditResponseDTO> getRecentAuditLogs() {
        return auditRepository.findTop10ByOrderByTimestampDesc()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Conversion method
    private AuditResponseDTO convertToResponseDTO(Audit audit) {
        AuditResponseDTO dto = new AuditResponseDTO();
        dto.setId(audit.getId());
        dto.setAction(audit.getAction());
        dto.setQuantity(audit.getQuantity());
        dto.setTimestamp(audit.getTimestamp());

        // User information
        if (audit.getUser() != null) {
            dto.setUserId(audit.getUser().getId());
            dto.setUsername(audit.getUser().getUsername());
        }

        // Product information
        if (audit.getProduct() != null) {
            dto.setProductId(audit.getProduct().getId());
            dto.setProductName(audit.getProduct().getName());
        }

        // Warehouse information
        if (audit.getWarehouse() != null) {
            dto.setWarehouseId(audit.getWarehouse().getId());
            dto.setWarehouseName(audit.getWarehouse().getName());
        }

        // Target warehouse information (for transfers)
        if (audit.getTargetWarehouse() != null) {
            dto.setTargetWarehouseId(audit.getTargetWarehouse().getId());
            dto.setTargetWarehouseName(audit.getTargetWarehouse().getName());
        }

        return dto;
    }
}