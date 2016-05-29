package ch.wisv.areafiftylan.service.repository.token;

import ch.wisv.areafiftylan.security.token.AuthenticationToken;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthenticationTokenRepository extends TokenRepository<AuthenticationToken> {
    Optional<AuthenticationToken> findByUserUsername(String username);
}
