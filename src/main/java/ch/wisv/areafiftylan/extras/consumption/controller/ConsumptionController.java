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

package ch.wisv.areafiftylan.extras.consumption.controller;

import ch.wisv.areafiftylan.exception.AlreadyConsumedException;
import ch.wisv.areafiftylan.exception.ConsumptionNotFoundException;
import ch.wisv.areafiftylan.extras.consumption.model.Consumption;
import ch.wisv.areafiftylan.extras.consumption.service.ConsumptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping(value = "/consumptions")
public class ConsumptionController {
    @Autowired
    private ConsumptionService consumptionService;

    @RequestMapping(value = "/{ticketId}", method = RequestMethod.GET)
    public Collection<Consumption> consumptionsMade(@PathVariable Long ticketId) {
        return consumptionService.getByTicketIdIfValid(ticketId).getConsumptionsMade();
    }

    @RequestMapping(method = RequestMethod.GET)
    public Collection<Consumption> getAllPossibleConsumptions() {
        return consumptionService.getPossibleConsumptions();
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> addAvailableConsumption(@RequestBody String consumptionName) {
        consumptionService.addPossibleConsumption(consumptionName);

        return createResponseEntity(HttpStatus.OK,
                "Successfully added " + consumptionName + " as a supported consumption.");
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<?> removeAvailableConsumption(@RequestBody Long consumptionId) {
        Consumption c = consumptionService.removePossibleConsumption(consumptionId);

        return createResponseEntity(HttpStatus.OK,
                "Successfully removed " + c.getName() + " as a supported consumption.");
    }

    @RequestMapping(value = "/{ticketId}/consume", method = RequestMethod.POST)
    public ResponseEntity<?> consume(@PathVariable Long ticketId, @RequestBody Long consumptionId) {
        consumptionService.consume(ticketId, consumptionId);
        return createResponseEntity(HttpStatus.OK, "Successfully consumed consumption");
    }

    @RequestMapping(value = "/{ticketId}/reset", method = RequestMethod.POST)
    public ResponseEntity<?> reset(@PathVariable Long ticketId, @RequestBody Long consumptionId) {
        consumptionService.reset(ticketId, consumptionId);
        return createResponseEntity(HttpStatus.OK, "Successfully reset consumption");
    }

    @ExceptionHandler(value = AlreadyConsumedException.class)
    public ResponseEntity<?> handleAlreadyConsumed(AlreadyConsumedException e) {
        return createResponseEntity(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(value = ConsumptionNotFoundException.class)
    public ResponseEntity<?> handleConsumptionNotSupported(ConsumptionNotFoundException e) {
        return createResponseEntity(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
