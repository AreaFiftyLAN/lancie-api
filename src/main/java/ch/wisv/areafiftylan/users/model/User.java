/*
 * Copyright (c) 2018  W.I.S.V. 'Christiaan Huygens'
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.wisv.areafiftylan.users.model;

import ch.wisv.areafiftylan.utils.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Entity
@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Table(name = "users", uniqueConstraints = { @UniqueConstraint(name = "email", columnNames = { "email" }) })
public class User implements Serializable, UserDetails {

    @NonNull
    @Column(nullable = false)
    @JsonView(View.NoProfile.class)
    @Email(message = "Email should be valid!")
    private String email;

    @NonNull
    @Getter(AccessLevel.NONE)
    @Column(nullable = false)
    private String passwordHash;

    @OneToOne(targetEntity = Profile.class, cascade = CascadeType.ALL)
    @JsonView(View.Public.class)
    private Profile profile = new Profile();

    @GeneratedValue
    @Id
    private Long id;

    @JsonIgnore
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_role")
    final private Set<Role> roles = new HashSet<>();

    @JsonIgnore
    private final boolean accountNonExpired = true;

    @JsonIgnore
    private boolean accountNonLocked = true;

    @JsonIgnore
    private final boolean credentialsNonExpired = true;

    @JsonIgnore
    private boolean enabled = true;
    
    // This method is created to allow logging in using the email field
    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    @Override
    public Set<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return passwordHash;
    }

    public void resetProfile() {
        this.profile = new Profile();
        this.profile.setBirthday(LocalDate.now());
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    @JsonView(View.Public.class)
    public int getReference() {
        return email.hashCode();
    }

    public void deleteRole(Role role) {
        this.roles.remove(role);
    }
}
