package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.ConsumptionDTO;
import ch.wisv.areafiftylan.exception.AlreadyConsumedException;
import ch.wisv.areafiftylan.exception.ConsumptionNotFoundException;
import ch.wisv.areafiftylan.model.ConsumptionMap;
import ch.wisv.areafiftylan.model.util.Consumption;
import ch.wisv.areafiftylan.service.ConsumptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

/**
 * Created by beer on 16-5-16.
 */
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping(value = "/consumptions")
public class ConsumptionController {
    @Autowired
    private ConsumptionService consumptionService;

    @RequestMapping(method = RequestMethod.GET)
    public boolean isConsumed(@RequestBody ConsumptionDTO consumptionDTO){
        return consumptionService.isConsumed(consumptionDTO.getTicketId(), consumptionDTO.getConsumptionId());
    }

    @RequestMapping(value = "/{ticketId}", method = RequestMethod.GET)
    public Collection<String> consumptionsMade(@PathVariable Long ticketId){
        List<String> consumptions = consumptionService.getByTicketId(ticketId).getConsumptionsMade().stream()
                .map(c -> c.getName())
                .collect(Collectors.toList());
        return consumptions;
    }

    @RequestMapping(value = "/available", method = RequestMethod.GET)
    public Collection<String> getAllPossibleConsumptions(){
        List<String> consumptions = consumptionService.getPossibleConsumptions().stream()
                .map(c -> c.getName())
                .collect(Collectors.toList());
        return consumptions;
    }

    @RequestMapping(value = "/available/{consumptionName}", method = RequestMethod.POST)
    public ResponseEntity<?> addAvailableConsumption(@PathVariable String consumptionName){
        consumptionService.addPossibleConsumption(consumptionName);

        return createResponseEntity(HttpStatus.OK, "Succesfully added " + consumptionName + " as a supported consumption.");
    }

    @RequestMapping(value = "/available/{consumptionId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeAvailableConsumption(@PathVariable Long consumptionId){
        Consumption c = consumptionService.removePossibleConsumption(consumptionId);

        return createResponseEntity(HttpStatus.OK, "Succesfully removed " + c.getName() + " as a supported consumption.");
    }

    @RequestMapping(value = "/consume", method = RequestMethod.POST)
    public ResponseEntity<?> consume(@RequestBody ConsumptionDTO consumptionDTO){
        consumptionService.consume(consumptionDTO.getTicketId(), consumptionDTO.getConsumptionId());
        return createResponseEntity(HttpStatus.OK, "Successfully consumed consumption");
    }

    @RequestMapping(value = "/reset", method = RequestMethod.POST)
    public ResponseEntity<?> reset(@RequestBody ConsumptionDTO consumptionDTO){
        consumptionService.reset(consumptionDTO.getTicketId(), consumptionDTO.getConsumptionId());
        return createResponseEntity(HttpStatus.OK, "Successfully reset consumption");
    }

    @ExceptionHandler(value = AlreadyConsumedException.class)
    public ResponseEntity<?> handleAlreadyConsumed(AlreadyConsumedException e){
        return createResponseEntity(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(value = ConsumptionNotFoundException.class)
    public ResponseEntity<?> handleConsumptionNotSupported(ConsumptionNotFoundException e){
        return createResponseEntity(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
