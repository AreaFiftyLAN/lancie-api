package ch.wisv.areafiftylan.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "teamName", columnNames = { "teamName" }) })
public class Team {

    @Id
    @GeneratedValue
    Long id;

    @Column(nullable = false)
    String teamName;

    @ManyToMany
    Set<User> members;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    User captain;

    int size;

    public Team(String teamName, User captain) {
        this.teamName = teamName;
        this.captain = captain;
        members = new HashSet<>();
        members.add(captain);
        size = 1;
    }

    public Team() {
        //jpa only
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Set<User> getMembers() {
        return members;
    }

    public void addMember(User member) {
        this.members.add(member);
    }

    public boolean removeMember(User member) {
        return this.members.remove(member);
    }

    public User getCaptain() {
        return captain;
    }

    public void setCaptain(User captain) {
        this.captain = captain;
    }

    public int getSize() {
        return members.size();
    }
}
