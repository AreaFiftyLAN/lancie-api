package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.util.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Created by sille on 22-12-15.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Collection<Ticket> findByPickupService_True();

    Integer countByType(TicketType type);

    Collection<Ticket> findAllByOwnerUsernameIgnoreCase(String username);
}
