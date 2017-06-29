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

package ch.wisv.areafiftylan.products.model.order;

import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.utils.view.View;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
public class Order {

    @Id
    @GeneratedValue
    @JsonView(View.OrderOverview.class)
    private Long id;

    @OneToMany(cascade = CascadeType.MERGE, targetEntity = Ticket.class, fetch = FetchType.EAGER)
    @JsonView(View.OrderOverview.class)
    private Set<Ticket> tickets;

    @JsonView(View.OrderOverview.class)
    @Setter
    private OrderStatus status;

    @JsonView(View.OrderOverview.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime creationDateTime;

    /**
     * This String can be used to store an external reference. Payment providers often have their own id.
     */
    @JsonView(View.OrderOverview.class)
    @Setter
    private String reference;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JsonView(View.OrderOverview.class)
    private User user = null;

    public Order() {
        this.status = OrderStatus.ANONYMOUS;
        this.creationDateTime = LocalDateTime.now();
        this.tickets = new HashSet<>();
    }

    public Order(User user) {
        this();
        this.user = user;
        this.status = OrderStatus.ASSIGNED;
    }

    public boolean addTicket(Ticket ticket) {
        return tickets.add(ticket);
    }

    public void clearTickets() {
        tickets.clear();
    }

    @JsonView(View.OrderOverview.class)
    public float getAmount() {
        float price = 0F;
        for (Ticket ticket : this.tickets) {
            price += ticket.getPrice();
        }
        return price;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.status = OrderStatus.ASSIGNED;
        } else {
            this.status = OrderStatus.ANONYMOUS;
        }
    }
}
