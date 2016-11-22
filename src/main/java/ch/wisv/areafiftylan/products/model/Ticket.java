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

package ch.wisv.areafiftylan.products.model;

import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.utils.view.View;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
public class Ticket {

    @Id
    @GeneratedValue
    @JsonView(View.OrderOverview.class)
    @Getter
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JsonView(View.Public.class)
    @Getter
    @Setter
    private User owner;

    @Enumerated(EnumType.STRING)
    @JsonView(View.OrderOverview.class)
    @Getter
    private TicketType type;

    @JsonView(View.OrderOverview.class)
    @Getter
    @Setter
    private String text;

    @JsonView(View.OrderOverview.class)
    @Getter
    @Setter
    private boolean pickupService;

    @JsonView(View.OrderOverview.class)
    @Getter
    @Setter
    private boolean chMember;

    @JsonView(View.OrderOverview.class)
    @Getter
    @Setter
    private boolean valid;

    public Ticket(User owner, TicketType type, Boolean pickupService, Boolean chMember) {
        this(type, pickupService, chMember);
        this.owner = owner;
    }

    public Ticket(TicketType type, Boolean pickupService, Boolean chMember) {
        this.owner = null;
        this.type = type;
        this.text = type.getText();
        this.pickupService = pickupService;
        this.chMember = chMember;
        this.valid = false;
    }

    public Ticket() {
        //JPA Only
    }

    @JsonView(View.OrderOverview.class)
    public float getPrice() {
        float finalPrice = type.getPrice();

        finalPrice += pickupService ? TicketOptions.PICKUPSERVICE.getPrice() : 0;

        finalPrice += chMember ? TicketOptions.CHMEMBER.getPrice() : 0;

        return finalPrice;
    }

    public boolean hasPickupService() {
        return pickupService;
    }
}
