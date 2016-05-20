package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.relations.RFIDLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by beer on 5-5-16.
 */
@Repository
public interface RFIDLinkRepository extends JpaRepository<RFIDLink, Long> {
    Optional<RFIDLink> findByRfid(String rfid);

    Optional<RFIDLink> findByTicketId(Long ticketId);
}
