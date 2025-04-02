package com.warehouse.repository;

import com.warehouse.model.Audit;
import com.warehouse.model.Product;
import com.warehouse.model.User;
import com.warehouse.model.Warehouse;
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

