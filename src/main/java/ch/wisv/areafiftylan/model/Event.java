package ch.wisv.areafiftylan.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Set;

/**
 * Created by Sille Kamoen on 27-3-16.
 */
@Entity
public class Event {

    @Id
    @GeneratedValue
    @Getter
    private Long id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private int teamSize;

    @Getter
    @Setter
    private int teamLimit;

    @Getter
    @ElementCollection(targetClass = Team.class)
    private Set<Team> registeredTeams;

    @Getter @Setter
    private boolean openForRegistration = false;

    public Event() {
        //JPA ONLY
    }

    public Event(String name, int teamSize, int teamLimit) {
        this.name = name;
        this.teamSize = teamSize;
        this.teamLimit = teamLimit;
    }

    public boolean addTeam(Team team) {
        if (registeredTeams.size() <= teamLimit || teamLimit == 0) {
            if (team.getSize() == teamSize) {
                return registeredTeams.add(team);
            }
        }
        return false;
    }
}
