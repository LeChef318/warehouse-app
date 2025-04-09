package ch.hoffmann.jan.warehouse.repository;

import ch.hoffmann.jan.warehouse.model.Audit;
import ch.hoffmann.jan.warehouse.model.Product;
import ch.hoffmann.jan.warehouse.model.User;
import ch.hoffmann.jan.warehouse.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    List<Audit> findByUser(User user);
    List<Audit> findByProduct(Product product);
    List<Audit> findByWarehouse(Warehouse warehouse);
    List<Audit> findTop10ByOrderByTimestampDesc();
}

