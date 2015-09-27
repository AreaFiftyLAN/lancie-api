package ch.wisv.areafiftylan.model;

import ch.wisv.areafiftylan.model.util.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;


@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "username", columnNames = { "username" }),
        @UniqueConstraint(name = "email", columnNames = { "email" }) })
public class User implements Serializable {

    @JsonIgnore
    @Column(nullable = false)
    public String passwordHash;

    @Column(nullable = false)
    public String username;

    @Column(nullable = false)
    public String email;

    @OneToOne(targetEntity = Profile.class, cascade = CascadeType.ALL)
    @JsonIgnore
    private Profile profile;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Collection<Role> roles;

    public User(String username, String passwordHash, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.profile = new Profile();
        this.roles = Collections.singletonList(Role.USER);
    }

    User() { // jpa only
    }

    public Profile getProfile() {
        return profile;
    }

    public Long getId() {
        return id;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void resetProfile() {
        this.profile = new Profile();
    }

    public Collection<Role> getRoles() {
        return roles;
    }

    public void addRole(Role role){
        this.roles.add(role);
    }

    public void removeRole(Role role){
        if(this.roles.contains(role)){
            this.roles.remove(role);
        }
    }
}
