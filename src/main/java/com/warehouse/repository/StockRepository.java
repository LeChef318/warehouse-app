package com.warehouse.repository;

import com.warehouse.model.Product;
import com.warehouse.model.Stock;
import com.warehouse.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByProduct(Product product);
    List<Stock> findByWarehouse(Warehouse warehouse);
    Optional<Stock> findByProductAndWarehouse(Product product, Warehouse warehouse);
}

