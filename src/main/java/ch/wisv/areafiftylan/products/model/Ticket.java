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

package ch.wisv.areafiftylan.products.model;

import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.utils.view.View;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    @JsonView(View.OrderOverview.class)
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JsonView(View.Public.class)
    private User owner;

    @JsonView(View.OrderOverview.class)
    @ManyToOne(cascade = CascadeType.MERGE)
    @Setter(AccessLevel.NONE)
    private TicketType type;


    @JsonView(View.OrderOverview.class)
    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @Setter(AccessLevel.NONE)
    private Set<TicketOption> enabledOptions;

    @JsonView(View.OrderOverview.class)
    private boolean valid;

    public Ticket(User owner, TicketType type) {
        this(type);
        this.owner = owner;
    }

    public Ticket(TicketType type) {
        this.owner = null;
        this.type = type;
        this.valid = false;
        this.enabledOptions = new HashSet<>();
    }

    public boolean addOption(TicketOption option) {
        return type.getPossibleOptions().contains(option) && enabledOptions.add(option);
    }

    @JsonView(View.OrderOverview.class)
    public float getPrice() {
        float finalPrice = type.getPrice();

        for (TicketOption ticketOption : enabledOptions) {
            finalPrice += ticketOption.getPrice();
        }

        return finalPrice;
    }
}
