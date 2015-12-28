package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Collection<Order> findAllByUserUsername(String username);

    Optional<Order> findByReference(String reference);
}
