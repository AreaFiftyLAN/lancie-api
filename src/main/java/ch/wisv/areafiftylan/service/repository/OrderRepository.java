package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Collection<Order> findAllByCreationDateTimeBefore(LocalDateTime creationDate);

    Collection<Order> findAllByUserUsername(String username);

    Optional<Order> findByReference(String reference);
}
