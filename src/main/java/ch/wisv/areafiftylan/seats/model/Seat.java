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

package ch.wisv.areafiftylan.seats.model;

import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.utils.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(uniqueConstraints = { @UniqueConstraint(name = "seatConstraint", columnNames = { "seatGroup", "seatNumber" }) })
public class Seat {

    @Id
    @GeneratedValue
    private Long Id;

    @JsonView(View.Public.class)
    private boolean locked = true;

    @OneToOne(cascade = CascadeType.MERGE)
    @JsonView(View.Public.class)
    private Ticket ticket;

    @NonNull
    @JsonView(View.Public.class)
    public String seatGroup;

    @JsonView(View.Public.class)
    public int seatNumber;

    public Seat(String seatGroup, int seatNumber) {
        this.seatGroup = seatGroup;
        this.seatNumber = seatNumber;
    }

    @JsonIgnore
    public User getUser() {
        return ticket.getOwner();
    }

    public boolean isTaken() {
        return ticket != null;
    }
}
