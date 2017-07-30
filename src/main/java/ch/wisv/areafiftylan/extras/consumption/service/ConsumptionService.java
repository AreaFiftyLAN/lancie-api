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

package ch.wisv.areafiftylan.extras.consumption.service;

import ch.wisv.areafiftylan.extras.consumption.model.Consumption;
import ch.wisv.areafiftylan.extras.consumption.model.ConsumptionMap;

import java.util.Collection;

public interface ConsumptionService {
    ConsumptionMap getByTicketIdIfValid(Long ticketId);

    boolean isConsumed(Long ticketId, Long consumptionId);

    void consume(Long ticketId, Long consumptionId);

    void reset(Long ticketId, Long consumptionId);

    Consumption getByConsumptionId(Long consumptionId);

    Collection<Consumption> getPossibleConsumptions();

    Collection<ConsumptionMap> getConsumptionMaps();

    void removePossibleConsumption(Long consumptionId);

    Consumption addPossibleConsumption(String consumptionName);
}
