package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.util.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by sille on 22-12-15.
 */
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByKey(String key);

    Optional<Ticket> findByOwnerUsername(String username);

    Collection<Ticket> findByPickupService_True();

    Long countByType(TicketType type);
}
