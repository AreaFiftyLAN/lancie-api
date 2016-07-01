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

package ch.wisv.areafiftylan.model;

import ch.wisv.areafiftylan.model.util.TicketOptions;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.model.view.View;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;

@Entity
public class Ticket {

    @Id
    @GeneratedValue
    @JsonView(View.OrderOverview.class)
    Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JsonView(View.Public.class)
    User owner;

    @Enumerated(EnumType.STRING)
    @JsonView(View.OrderOverview.class)
    TicketType type;

    @JsonView(View.OrderOverview.class)
    String text;

    @JsonView(View.OrderOverview.class)
    boolean pickupService;

    @JsonView(View.OrderOverview.class)
    boolean chMember;

    @JsonView(View.OrderOverview.class)
    boolean valid;

    @JsonView(View.OrderOverview.class)
    float price;

    public Ticket(User owner, TicketType type, Boolean pickupService, Boolean chMember) {
        this.owner = owner;
        this.type = type;
        this.text = type.getText();
        this.pickupService = pickupService;
        this.chMember = chMember;
        this.valid = false;

        price = getPrice();
    }

    public Ticket() {
        //JPA Only
    }

    public Long getId(){ return id; }

    public boolean isPickupService() {
        return pickupService;
    }

    public void setPickupService(boolean pickupService) {
        this.pickupService = pickupService;
        price = getPrice();
    }

    public boolean isChMember() {
        return chMember;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public TicketType getType() {
        return type;
    }

    public float getPrice() {
        float finalPrice = type.getPrice();

        finalPrice += pickupService ? TicketOptions.PICKUPSERVICE.getPrice() : 0;

        finalPrice += chMember ? TicketOptions.CHMEMBER.getPrice() : 0;

        return finalPrice;
    }

    public boolean hasPickupService() {
        return pickupService;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
