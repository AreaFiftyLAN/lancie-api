package ch.wisv.areafiftylan.service.repository.token;

import ch.wisv.areafiftylan.security.token.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Created by beer on 4-4-16.
 */
public interface TokenRepository<T extends Token> extends JpaRepository<T, Long> {
    Optional<T> findByToken(String token);
}