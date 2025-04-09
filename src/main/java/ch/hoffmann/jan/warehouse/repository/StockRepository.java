package ch.hoffmann.jan.warehouse.repository;

import ch.hoffmann.jan.warehouse.model.Product;
import ch.hoffmann.jan.warehouse.model.Stock;
import ch.hoffmann.jan.warehouse.model.Warehouse;
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

