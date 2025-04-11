package ch.hoffmann.jan.warehouse.repository;

import ch.hoffmann.jan.warehouse.model.Audit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    List<Audit> findTop10ByOrderByTimestampDesc();
    Page<Audit> findAllByOrderByTimestampDesc(Pageable pageable);
}