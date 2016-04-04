package ch.wisv.areafiftylan.service.repository.token;

import ch.wisv.areafiftylan.security.token.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends TokenRepository<VerificationToken> {
    List<VerificationToken> findAllByExpiryDateBefore(LocalDateTime date);
}
