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

package ch.wisv.areafiftylan.seats.model;

import ch.wisv.areafiftylan.utils.view.View;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.users.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "seatConstraint", columnNames = { "seatGroup", "seatNumber" }) })
public class Seat {

    @Getter
    @Setter
    @JsonView(View.Public.class)
    public boolean taken;

    @Getter
    @OneToOne(cascade = CascadeType.MERGE)
    @JsonView(View.Public.class)
    public Ticket ticket;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    @Getter
    @Setter
    @JsonView(View.Public.class)
    public String seatGroup;

    @Getter
    @Setter
    @JsonView(View.Public.class)
    public int seatNumber;

    public Seat(String seatGroup, int seatNumber) {
        this.seatGroup = seatGroup;
        this.seatNumber = seatNumber;
        this.taken = false;
    }

    public Seat() {
        //JPA ONLY
    }

    @JsonIgnore
    public User getUser() {
        return ticket.getOwner();
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;

        this.taken = this.ticket != null;
    }
}
