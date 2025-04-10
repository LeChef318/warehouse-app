package ch.hoffmann.jan.warehouse.repository;

import ch.hoffmann.jan.warehouse.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByRole(String role);
    List<User> findByActiveTrue();
    long countByRoleAndActiveTrue(String role);
}

