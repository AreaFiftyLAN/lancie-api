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

import ch.wisv.areafiftylan.utils.view.View;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class TicketType {
    //    EARLY_FULL("Early Bird", 37.50F, 50, LocalDateTime.of(2016, 6, 3, 0, 0), true),
    //    REGULAR_FULL("Regular", 40.00F, 0, LocalDateTime.of(2016, 5, 28, 23, 59), true),
    //    LAST_MINUTE("Last Minute", 42.50F, 0, LocalDateTime.of(2016, 6, 5, 23, 59), true),
    //    TEST("Test Ticket", 999.0F, 0, LocalDateTime.MAX, true),
    //    FREE("Free", 0F, 0, LocalDateTime.MAX, false);

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @JsonView(View.OrderOverview.class)
    private String name;

    @NonNull
    @JsonView(View.OrderOverview.class)
    private String text;

    private float price;

    /**
     * The maximum amount of tickets of this type that can be sold.
     * Will be 0 if no maximum amount is set.
     */
    private int numberAvailable;

    /**
     * A time after which no more tickets of this type can be sold.
     * Will be null if no deadline is set.
     */
    private LocalDateTime deadline;

    private boolean buyable;
    
    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private Set<TicketOption> possibleOptions;

    public TicketType(String name, String text, float price, int numberAvailable, LocalDateTime deadline, boolean buyable) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TicketType that = (TicketType) o;

        if (Float.compare(that.price, price) != 0) {
            return false;
        }
        if (numberAvailable != that.numberAvailable) {
            return false;
        }
        if (buyable != that.buyable) {
            return false;
        }
        if (!id.equals(that.id)) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }
        if (!text.equals(that.text)) {
            return false;
        }
        return possibleOptions != null ? possibleOptions.equals(that.possibleOptions) : that.possibleOptions == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + text.hashCode();
        result = 31 * result + (price != +0.0f ? Float.floatToIntBits(price) : 0);
        result = 31 * result + numberAvailable;
        result = 31 * result + (buyable ? 1 : 0);
        result = 31 * result + (possibleOptions != null ? possibleOptions.hashCode() : 0);
        return result;
    }
}
