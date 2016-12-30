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

import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;

public class TicketInformationResponse {
    @Getter
    private String ticketType;

    @Getter
    private int limit;

    @Getter
    private int numberSold;

    @Getter
    private double price;

    @Getter
    private String text;

    @Getter
    private HashMap<String, Float> options;

    @Getter
    private String deadline;

    public TicketInformationResponse(TicketType type, Collection<TicketOption> options, int numberSold) {
        this.ticketType = type.getName();
        this.limit = type.getNumberAvailable();
        this.numberSold = numberSold;
        this.price = type.getPrice();
        this.text = type.getText();
        this.options = new HashMap<>(options.size());
        this.deadline = type.getDeadline().toString();

        for (TicketOption option : options) {
            if (type.getPossibleOptions().contains(option)) {
                this.options.put(option.getName(), option.getPrice());
            }
        }
    }
}
