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

package ch.wisv.areafiftylan.extras.consumption.model;

import lombok.Getter;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import javax.persistence.Entity;

/**
 * Created by beer on 20-5-16.
 */
@Entity
public class Consumption {
    @GeneratedValue
    @Getter
    @Id
    Long id;

    @Getter
    String name;

    public Consumption() {
        // JPA Only
    }

    public Consumption(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Consumption)) return false;

        Consumption that = (Consumption) o;

        if (id != that.id) return false;
        return name.equals(that.getName());

    }
}
