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

import ch.wisv.areafiftylan.utils.view.View;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
public class TicketType {
    //    EARLY_FULL("Early Bird", 37.50F, 50, LocalDateTime.of(2016, 6, 3, 0, 0), true),
    //    REGULAR_FULL("Regular", 40.00F, 0, LocalDateTime.of(2016, 5, 28, 23, 59), true),
    //    LAST_MINUTE("Last Minute", 42.50F, 0, LocalDateTime.of(2016, 6, 5, 23, 59), true),
    //    TEST("Test Ticket", 999.0F, 0, LocalDateTime.MAX, true),
    //    FREE("Free", 0F, 0, LocalDateTime.MAX, false);

    @Id
    @GeneratedValue
    @Getter
    private Long id;

    @Getter
    @Setter
    @JsonView(View.OrderOverview.class)
    private String name;

    @Getter
    @Setter
    private float price;

    /**
     * The maximum amount of tickets of this type that can be sold.
     * Will be 0 if no maximum amount is set.
     */
    @Getter
    @Setter
    private int numberAvailable;

    @Getter
    @Setter
    @JsonView(View.OrderOverview.class)
    private String text;

    /**
     * A time after which no more tickets of this type can be sold.
     * Will be null if no deadline is set.
     */
    @Getter
    @Setter
    private LocalDateTime deadline;

    @Getter
    @Setter
    private boolean buyable;
    
    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @Getter
    Set<TicketOption> possibleOptions;

    public TicketType(String name, String text, float price, int numberAvailable, LocalDateTime deadline,
                      boolean buyable) {
        this.name = name;
        this.text = text;
        this.price = price;
        this.numberAvailable = numberAvailable;
        this.deadline = deadline;
        this.buyable = buyable;
        this.possibleOptions = new HashSet<>();
    }

    public void addPossibleOption(TicketOption option) {
        this.possibleOptions.add(option);
    }
}
