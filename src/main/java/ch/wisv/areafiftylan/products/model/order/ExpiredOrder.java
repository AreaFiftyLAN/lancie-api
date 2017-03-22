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

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

/**
 * The ExpiredOrder class is made to keep track of expired Orders. When an order is expired, all the relevant data is
 * stored in String format for reference.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ExpiredOrder {
    @Id
    private Long id;

    private int numberOfTickets;

    private String createdAt;

    private String expiredAt;

    private String createdBy;

    public ExpiredOrder(Order order) {
        this.id = order.getId();
        this.numberOfTickets = order.getTickets().size();
        this.createdAt = order.getCreationDateTime().toString();
        this.expiredAt = LocalDateTime.now().toString();
        if (order.getUser() != null) {
            this.createdBy = order.getUser().getUsername();
        } else {
            this.createdBy = "Anonymous";
        }
    }
}
