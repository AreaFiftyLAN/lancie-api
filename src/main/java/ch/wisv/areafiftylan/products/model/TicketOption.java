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
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class TicketOption {

    @Id
    @GeneratedValue
    private Long id;

    @JsonView(View.OrderOverview.class)
    private String name;

    @JsonView(View.OrderOverview.class)
    private float price;

    public TicketOption(String name, float price) {
        this.name = name;
        this.price = price;
    }
}
