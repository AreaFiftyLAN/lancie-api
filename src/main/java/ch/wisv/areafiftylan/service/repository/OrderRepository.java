package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Collection<Order> findAllByUserUsername(String username);

    Optional<Order> findByReference(String reference);
}
