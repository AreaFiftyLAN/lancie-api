package ch.wisv.areafiftylan.model;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by Sille Kamoen on 27-3-16.
 */
@Entity
public class Event {

    @Column(unique = true)
    private String eventName;

    @Id
    @GeneratedValue
    private Long id;

    private int teamSize;

    private int teamLimit;

    @ElementCollection(targetClass = Team.class)
    private Set<Team> registeredTeams;

    private boolean openForRegistration = false;

    public Event() {
        //JPA ONLY
    }

    public Event(String eventName, int teamSize, int teamLimit) {
        this.eventName = eventName;
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

    public Long getId() {
        return id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public int getTeamLimit() {
        return teamLimit;
    }

    public void setTeamLimit(int teamLimit) {
        this.teamLimit = teamLimit;
    }

    public Set<Team> getRegisteredTeams() {
        return registeredTeams;
    }

    public boolean isOpenForRegistration() {
        return openForRegistration;
    }

    public void setOpenForRegistration(boolean openForRegistration) {
        this.openForRegistration = openForRegistration;
    }
}
