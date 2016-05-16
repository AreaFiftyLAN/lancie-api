package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.ConsumptionDTO;
import ch.wisv.areafiftylan.exception.AlreadyConsumedException;
import ch.wisv.areafiftylan.exception.ConsumptionNotSupportedException;
import ch.wisv.areafiftylan.model.ConsumptionMap;
import ch.wisv.areafiftylan.service.ConsumptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

/**
 * Created by beer on 16-5-16.
 */
@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping(value = "/consumptions")
public class ConsumptionController {
    @Autowired
    private ConsumptionService consumptionService;

    @RequestMapping(method = RequestMethod.GET)
    public boolean isConsumed(@RequestBody ConsumptionDTO consumptionDTO){
        return consumptionService.getByTicketId(consumptionDTO.getTicketId()).isConsumed(consumptionDTO.getConsumption());
    }

    @RequestMapping(value = "/{ticketId}", method = RequestMethod.GET)
    public Collection<String> consumptionsMade(@PathVariable Long ticketId){
        return consumptionService.getByTicketId(ticketId).getConsumptionsMade();
    }

    @RequestMapping(value = "/available", method = RequestMethod.GET)
    public Collection<String> getAllPossibleConsumptions(){
        return consumptionService.getPossibleConsumptions();
    }

    @RequestMapping(value = "/available/{consumption}", method = RequestMethod.POST)
    public ResponseEntity<?> addAvailableConsumption(@PathVariable String consumption){
        consumptionService.addPossibleConsumption(consumption);

        return createResponseEntity(HttpStatus.OK, "Succesfully added " + consumption + " as a supported consumption.");
    }

    @RequestMapping(value = "/available/{consumption}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeAvailableConsumption(@PathVariable String consumption){
        consumptionService.removePossibleConsumption(consumption);

        return createResponseEntity(HttpStatus.OK, "Succesfully removed " + consumption + " as a supported consumption.");
    }

    @RequestMapping(value = "/consume", method = RequestMethod.POST)
    public ResponseEntity<?> consume(@RequestBody ConsumptionDTO consumptionDTO){
        consumptionService.consume(consumptionDTO.getTicketId(), consumptionDTO.getConsumption());
        return createResponseEntity(HttpStatus.OK, "Successfully consumed " + consumptionDTO.getConsumption());
    }

    @RequestMapping(value = "/reset", method = RequestMethod.POST)
    public ResponseEntity<?> reset(@RequestBody ConsumptionDTO consumptionDTO){
        consumptionService.reset(consumptionDTO.getTicketId(), consumptionDTO.getConsumption());
        return createResponseEntity(HttpStatus.OK, "Successfully reset consumption " + consumptionDTO.getConsumption());
    }

    @ExceptionHandler(value = AlreadyConsumedException.class)
    public ResponseEntity<?> handleAlreadyConsumed(AlreadyConsumedException e){
        return createResponseEntity(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(value = ConsumptionNotSupportedException.class)
    public ResponseEntity<?> handleConsumptionNotSupported(ConsumptionNotSupportedException e){
        return createResponseEntity(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
