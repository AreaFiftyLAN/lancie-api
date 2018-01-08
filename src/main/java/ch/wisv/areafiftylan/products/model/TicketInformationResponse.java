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

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class TicketInformationResponse {

    private String ticketType;

    private int limit;

    private int numberSold;

    private double price;

    private String text;

    private Map<String, Float> possibleOptions;

    private String deadline;

    public TicketInformationResponse(TicketType type, int numberSold) {
        this.ticketType = type.getName();
        this.limit = type.getNumberAvailable();
        this.numberSold = numberSold;
        this.price = type.getPrice();
        this.text = type.getText();
        this.deadline = type.getDeadline().toString();

        this.possibleOptions = new HashMap<>(type.getPossibleOptions().size());
        for (TicketOption option : type.getPossibleOptions()) {
            this.possibleOptions.put(option.getName(), option.getPrice());
        }
    }
}
