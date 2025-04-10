package ch.hoffmann.jan.warehouse.repository;

import ch.hoffmann.jan.warehouse.model.Audit;
import ch.hoffmann.jan.warehouse.model.Product;
import ch.hoffmann.jan.warehouse.model.User;
import ch.hoffmann.jan.warehouse.model.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    List<Audit> findByUser(User user);
    List<Audit> findByProduct(Product product);
    List<Audit> findByWarehouse(Warehouse warehouse);
    List<Audit> findTop10ByOrderByTimestampDesc();

    Page<Audit> findByUserOrderByTimestampDesc(User user, Pageable pageable);
    Page<Audit> findByProductOrderByTimestampDesc(Product product, Pageable pageable);
    Page<Audit> findByWarehouseOrderByTimestampDesc(Warehouse warehouse, Pageable pageable);
    Page<Audit> findByActionOrderByTimestampDesc(String action, Pageable pageable);
    Page<Audit> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT a FROM Audit a WHERE " +
            "(:userId IS NULL OR a.user.id = :userId) AND " +
            "(:productId IS NULL OR a.product.id = :productId) AND " +
            "(:warehouseId IS NULL OR a.warehouse.id = :warehouseId OR a.targetWarehouse.id = :warehouseId) AND " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR a.timestamp <= :endDate) " +
            "ORDER BY a.timestamp DESC")
    Page<Audit> findByFilters(
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId,
            @Param("action") String action,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}