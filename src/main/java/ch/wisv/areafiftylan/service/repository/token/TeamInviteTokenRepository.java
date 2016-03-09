package ch.wisv.areafiftylan.service.repository.token;

import ch.wisv.areafiftylan.security.TeamInviteToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by Sille Kamoen on 9-3-16.
 */
@Repository
public interface TeamInviteTokenRepository extends JpaRepository<TeamInviteToken, Long> {

    Optional<TeamInviteToken> findByToken(String token);

    Collection<TeamInviteToken> findByUserUsername(String username);


}
