package ch.hoffmann.jan.warehouse.repository;

import ch.hoffmann.jan.warehouse.model.Category;
import ch.hoffmann.jan.warehouse.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(Category category);
    boolean existsByName(String name);
}

