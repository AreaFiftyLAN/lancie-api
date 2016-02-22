package ch.wisv.areafiftylan.model;

import ch.wisv.areafiftylan.model.util.Role;
import ch.wisv.areafiftylan.model.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;


@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "username", columnNames = { "username" }),
        @UniqueConstraint(name = "email", columnNames = { "email" }) })
public class User implements Serializable, UserDetails {

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    @JsonView(View.NoProfile.class)
    protected String username;

    @Column(nullable = false)
    @JsonView(View.NoProfile.class)
    protected String email;

    @OneToOne(targetEntity = Profile.class, cascade = CascadeType.ALL)
    @JsonView(View.Public.class)
    protected Profile profile;

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    @JsonView(View.NoProfile.class)
    private Long id;

    @JsonIgnore
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_role")
    private Collection<Role> roles;

    @JsonIgnore
    boolean accountNonExpired = true;
    @JsonIgnore
    boolean accountNonLocked = true;
    @JsonIgnore
    boolean credentialsNonExpired = true;
    @JsonIgnore
    boolean enabled = true;

    public User(String username, String passwordHash, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.profile = new Profile();
        this.roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
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
    @JsonIgnore
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

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonView(View.Public.class)
    public int getReference() {
        return username.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;

        if (!username.equals(user.username)) {
            return false;
        }
        if (!email.equals(user.email)) {
            return false;
        }
        return id.equals(user.id);

    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }
}
