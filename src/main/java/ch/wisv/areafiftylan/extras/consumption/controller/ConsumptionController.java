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
import ch.wisv.areafiftylan.extras.consumption.model.ConsumptionMap;
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
@RequestMapping("/consumptions")
public class ConsumptionController {
    private final ConsumptionService consumptionService;

    @Autowired
    public ConsumptionController(ConsumptionService consumptionService) {
        this.consumptionService = consumptionService;
    }

    @GetMapping
    public Collection<Consumption> getAllPossibleConsumptions() {
        return consumptionService.getPossibleConsumptions();
    }

    @GetMapping("/consumptionMaps")
    public Collection<ConsumptionMap> getAllConsumptionMaps() {
        return consumptionService.getConsumptionMaps();
    }

    @PostMapping
    public ResponseEntity<?> addAvailableConsumption(@RequestBody String consumptionName) {
        consumptionService.addPossibleConsumption(consumptionName);

        return createResponseEntity(HttpStatus.OK,
                "Successfully added " + consumptionName + " as a supported consumption.");
    }

    @DeleteMapping("/{consumptionId}")
    public ResponseEntity<?> removeAvailableConsumption(@PathVariable Long consumptionId) {
        consumptionService.removePossibleConsumption(consumptionId);
        return createResponseEntity(HttpStatus.OK,
                "Successfully removed the supported consumption with id: " + consumptionId);
    }

    @GetMapping("/{ticketId}")
    public Collection<Consumption> consumptionsMade(@PathVariable Long ticketId) {
        return consumptionService.getByTicketIdIfValid(ticketId).getConsumptionsMade();
    }

    @PostMapping("/{ticketId}/consume")
    public ResponseEntity<?> consume(@PathVariable Long ticketId, @RequestBody Long consumptionId) {
        consumptionService.consume(ticketId, consumptionId);
        return createResponseEntity(HttpStatus.OK, "Successfully consumed consumption");
    }

    @PostMapping("/{ticketId}/reset")
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
