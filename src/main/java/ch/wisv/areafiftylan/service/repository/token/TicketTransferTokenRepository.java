package ch.wisv.areafiftylan.service.repository.token;

import ch.wisv.areafiftylan.security.token.TicketTransferToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface TicketTransferTokenRepository extends TokenRepository<TicketTransferToken> {

    Collection<TicketTransferToken> findAllByTicketId(Long ticketId);

    Collection<TicketTransferToken> findAllByTicketOwnerUsername(String username);
}
