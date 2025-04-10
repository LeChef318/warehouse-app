package ch.hoffmann.jan.warehouse.service;

import ch.hoffmann.jan.warehouse.dto.audit.AuditFilterDTO;
import ch.hoffmann.jan.warehouse.dto.audit.AuditResponseDTO;
import ch.hoffmann.jan.warehouse.exception.WarehouseException;
import ch.hoffmann.jan.warehouse.model.Audit;
import ch.hoffmann.jan.warehouse.model.AuditAction;
import ch.hoffmann.jan.warehouse.model.Product;
import ch.hoffmann.jan.warehouse.model.User;
import ch.hoffmann.jan.warehouse.model.Warehouse;
import ch.hoffmann.jan.warehouse.repository.AuditRepository;
import ch.hoffmann.jan.warehouse.repository.ProductRepository;
import ch.hoffmann.jan.warehouse.repository.UserRepository;
import ch.hoffmann.jan.warehouse.repository.WarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    public AuditService(AuditRepository auditRepository, UserRepository userRepository,
                        ProductRepository productRepository, WarehouseRepository warehouseRepository) {
        this.auditRepository = auditRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public Page<AuditResponseDTO> getAuditLogs(AuditFilterDTO filterDTO) {
        Pageable pageable = PageRequest.of(filterDTO.getPage(), filterDTO.getSize());

        // Validate action if provided
        if (filterDTO.getAction() != null && !filterDTO.getAction().isEmpty() && !AuditAction.isValid(filterDTO.getAction())) {
            throw new WarehouseException.InvalidAuditActionException(filterDTO.getAction());
        }

        Page<Audit> auditPage = auditRepository.findByFilters(
                filterDTO.getUserId(),
                filterDTO.getProductId(),
                filterDTO.getWarehouseId(),
                filterDTO.getAction(),
                filterDTO.getStartDate(),
                filterDTO.getEndDate(),
                pageable);

        return auditPage.map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<AuditResponseDTO> getRecentAuditLogs() {
        return auditRepository.findTop10ByOrderByTimestampDesc().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AuditResponseDTO createAuditLog(Long userId, Long productId, Long warehouseId, Long targetWarehouseId, String action, Integer quantity) {
        // Validate action
        if (!AuditAction.isValid(action)) {
            throw new WarehouseException.InvalidAuditActionException(action);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("User", "id", userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Product", "id", productId));

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Warehouse", "id", warehouseId));

        Warehouse targetWarehouse = null;
        if (targetWarehouseId != null) {
            targetWarehouse = warehouseRepository.findById(targetWarehouseId)
                    .orElseThrow(() -> new WarehouseException.ResourceNotFoundException("Target Warehouse", "id", targetWarehouseId));
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

        logger.info("Created audit log: User {} {} {} units of product {} in warehouse {}{}",
                user.getUsername(),
                action.toLowerCase(),
                quantity,
                product.getName(),
                warehouse.getName(),
                targetWarehouse != null ? " to warehouse " + targetWarehouse.getName() : "");

        return convertToResponseDTO(savedAudit);
    }

    /**
     * Converts an Audit entity to an AuditResponseDTO
     */
    private AuditResponseDTO convertToResponseDTO(Audit audit) {
        AuditResponseDTO dto = new AuditResponseDTO();
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