/*
 * Copyright (c) 2016  W.I.S.V. 'Christiaan Huygens'
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

package ch.wisv.areafiftylan.teams.model;

import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.utils.view.View;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(uniqueConstraints = { @UniqueConstraint(name = "teamName", columnNames = { "teamName" }) })
public class Team {

    @JsonView(View.Public.class)
    @Id
    @GeneratedValue
    private Long id;

    @JsonView(View.Public.class)
    @Column(nullable = false)
    private String teamName;

    @JsonView(View.Public.class)
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<User> members;

    @JsonView(View.Public.class)
    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    private User captain;

    @JsonView(View.Public.class)
    private int size;

    public Team(String teamName, User captain) {
        this.teamName = teamName;
        this.captain = captain;
        members = new HashSet<>();
        members.add(captain);
        size = 1;
    }

    public boolean addMember(User member) {
        return this.members.add(member);
    }

    public boolean removeMember(User member) {
        return this.members.remove(member);
    }

    public int getSize() {
        return members.size();
    }
}
