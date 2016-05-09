package ch.wisv.areafiftylan.service.repository.token;

import ch.wisv.areafiftylan.security.token.TicketTransferToken;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface TicketTransferTokenRepository extends TokenRepository<TicketTransferToken> {

    Collection<TicketTransferToken> findAllByTicketId(Long ticketId);

    Collection<TicketTransferToken> findAllByTicketOwnerUsernameIgnoreCase(String username);
}
