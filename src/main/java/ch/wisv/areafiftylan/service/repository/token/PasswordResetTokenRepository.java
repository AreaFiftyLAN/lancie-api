package ch.wisv.areafiftylan.service.repository.token;

import ch.wisv.areafiftylan.security.token.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends TokenRepository<PasswordResetToken> {
}
