package ch.wisv.areafiftylan.model;

import ch.wisv.areafiftylan.model.util.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;


@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "username", columnNames = { "username" }),
        @UniqueConstraint(name = "email", columnNames = { "email" }) })
public class User implements Serializable, UserDetails {

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

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_role")
    private Collection<Role> roles;

    boolean accountNonExpired = false;
    boolean accountNonLocked = false;
    boolean credentialsNonExpired = false;
    boolean enabled;

    public User(String username, String passwordHash, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.profile = new Profile();
        this.roles = new HashSet<>();
        roles.add(Role.USER);
    }

    User() { // jpa only
    }

    public Profile getProfile() {
        return profile;
    }

    public Long getId() {
        return id;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
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

    public void addRole(Role role) {
        this.roles.add(role);
    }
}
