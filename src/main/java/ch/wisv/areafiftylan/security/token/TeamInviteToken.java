package ch.wisv.areafiftylan.security.token;

import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Created by Sille Kamoen on 9-3-16.
 */
@Entity
public class TeamInviteToken extends Token {

    @OneToOne(targetEntity = Team.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private Team team;

    public TeamInviteToken() {
        //JPA Only
    }

    public TeamInviteToken(User user, Team team){
        super(user, 0);
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }
}
