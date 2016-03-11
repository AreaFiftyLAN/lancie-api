package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.security.TicketTransferToken;
import ch.wisv.areafiftylan.security.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketTransferTokenRepository extends JpaRepository<TicketTransferToken, Long> {
    Optional<TicketTransferToken> findByToken(String token);
}
