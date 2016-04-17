package ch.wisv.areafiftylan.model;

import ch.wisv.areafiftylan.model.view.View;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Sille Kamoen on 27-3-16.
 */
@Entity
public class Event {

    @JsonView(View.Public.class)
    private String eventName;

    @JsonView(View.Public.class)
    @Id
    @GeneratedValue
    private Long id;

    @JsonView(View.Public.class)
    private int teamSize;

    @JsonView(View.Public.class)
    private int teamLimit;

    @JsonView(View.Public.class)
    @ElementCollection(targetClass = Team.class, fetch = FetchType.EAGER)
    private Set<Team> registeredTeams;

    @JsonView(View.Public.class)
    private boolean openForRegistration = false;

    public Event() {
        //JPA ONLY
    }

    public Event(String eventName, int teamSize, int teamLimit) {
        this.eventName = eventName;
        this.teamSize = teamSize;
        this.teamLimit = teamLimit;
        this.registeredTeams = new HashSet<>(teamLimit);
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
