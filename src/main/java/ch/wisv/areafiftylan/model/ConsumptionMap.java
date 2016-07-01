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

import ch.wisv.areafiftylan.exception.AlreadyConsumedException;
import ch.wisv.areafiftylan.model.util.Consumption;
import lombok.Getter;
import lombok.NonNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by beer on 8-5-16.
 */
@Entity
public class ConsumptionMap {
    @Id
    @GeneratedValue
    Long id;

    @NonNull
    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Consumption> consumptionsMade;

    @OneToOne(targetEntity = Ticket.class, cascade = CascadeType.MERGE)
    @NonNull
    @Getter
    private Ticket ticket;

    public ConsumptionMap() {
        // JPA Only
    }

    public ConsumptionMap(Ticket t) {
        this.consumptionsMade = new ArrayList<>();
        this.ticket = t;
    }

    public boolean isConsumed(Consumption consumption){
        return consumptionsMade.contains(consumption);
    }

    public void consume(Consumption consumption){
        if(isConsumed(consumption)){
            throw new AlreadyConsumedException(consumption);
        }

        consumptionsMade.add(consumption);
    }

    public void reset(Consumption consumption){
        if(isConsumed(consumption)){
            consumptionsMade.remove(consumption);
        }
    }

    public Collection<Consumption> getConsumptionsMade(){
        return consumptionsMade;
    }
}
