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

import ch.wisv.areafiftylan.exception.ConsumptionNotFoundException;
import ch.wisv.areafiftylan.exception.InvalidTicketException;
import ch.wisv.areafiftylan.extras.consumption.model.Consumption;
import ch.wisv.areafiftylan.extras.consumption.model.ConsumptionMap;
import ch.wisv.areafiftylan.extras.consumption.model.ConsumptionMapsRepository;
import ch.wisv.areafiftylan.extras.consumption.model.PossibleConsumptionsRepository;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConsumptionServiceImpl implements ConsumptionService {
    private final ConsumptionMapsRepository consumptionMapsRepository;
    private final PossibleConsumptionsRepository possibleConsumptionsRepository;
    private final TicketService ticketService;

    @Autowired
    public ConsumptionServiceImpl(ConsumptionMapsRepository consumptionMapsRepository,
                                  PossibleConsumptionsRepository possibleConsumptionsRepository,
                                  TicketService ticketService) {
        this.consumptionMapsRepository = consumptionMapsRepository;
        this.possibleConsumptionsRepository = possibleConsumptionsRepository;
        this.ticketService = ticketService;
    }

    @Override
    public ConsumptionMap getByTicketIdIfValid(Long ticketId) {
        if (!ticketService.getTicketById(ticketId).isValid()) {
            throw new InvalidTicketException("Ticket is invalid; It can not be used for consumptions.");
        }

        Optional<ConsumptionMap> mapOptional = consumptionMapsRepository.findByTicketId(ticketId);

        if (mapOptional.isPresent()) {
            return mapOptional.get();
        } else {
            return initializeConsumptionMap(ticketId);
        }
    }

    @Override
    public boolean isConsumed(Long ticketId, Long consumptionId) {
        Consumption c = getByConsumptionId(consumptionId);
        return getByTicketIdIfValid(ticketId).isConsumed(c);
    }

    private ConsumptionMap initializeConsumptionMap(Long ticketId) {
        Ticket t = ticketService.getTicketById(ticketId);
        return consumptionMapsRepository.saveAndFlush(new ConsumptionMap(t));
    }

    @Override
    public void consume(Long ticketId, Long consumptionId) {
        ConsumptionMap consumptions = getByTicketIdIfValid(ticketId);
        Consumption consumption = getByConsumptionId(consumptionId);
        consumptions.consume(consumption);
        consumptionMapsRepository.saveAndFlush(consumptions);
    }

    @Override
    public void reset(Long ticketId, Long consumptionId) {
        ConsumptionMap consumptions = getByTicketIdIfValid(ticketId);
        Consumption consumption = getByConsumptionId(consumptionId);
        consumptions.reset(consumption);
        consumptionMapsRepository.saveAndFlush(consumptions);
    }

    @Override
    public Consumption getByConsumptionId(Long consumptionId) {
        return possibleConsumptionsRepository.findById(consumptionId)
                .orElseThrow(() -> new ConsumptionNotFoundException(consumptionId));
    }

    @Override
    public Collection<Consumption> getPossibleConsumptions() {
        return possibleConsumptionsRepository.findAll();
    }

    @Override
    public Consumption removePossibleConsumption(Long consumptionId) {
        Consumption consumption = getByConsumptionId(consumptionId);

        resetConsumptionEverywhere(consumption);

        possibleConsumptionsRepository.delete(consumption);
        return consumption;
    }

    @Override
    public Consumption addPossibleConsumption(String consumptionName) {
        if (possibleConsumptionsRepository.findByName(consumptionName).isPresent()) {
            throw new DuplicateKeyException("Consumption " + consumptionName + " is already supported");
        }

        Consumption consumption = new Consumption(consumptionName);
        return possibleConsumptionsRepository.saveAndFlush(consumption);
    }

    private void resetConsumptionEverywhere(Consumption consumption) {
        Collection<Ticket> allValidTickets =
                ticketService.getAllTickets().stream().filter(Ticket::isValid).collect(Collectors.toList());

        allValidTickets.forEach(t -> reset(t.getId(), consumption.getId()));
    }
}
