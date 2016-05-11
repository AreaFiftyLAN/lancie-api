package ch.wisv.areafiftylan.service.repository.token;

import ch.wisv.areafiftylan.security.token.TeamInviteToken;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Created by Sille Kamoen on 9-3-16.
 */
@Repository
public interface TeamInviteTokenRepository extends TokenRepository<TeamInviteToken> {

    Collection<TeamInviteToken> findByUserUsernameIgnoreCase(String username);

    Collection<TeamInviteToken> findByTeamId(Long teamId);


}
