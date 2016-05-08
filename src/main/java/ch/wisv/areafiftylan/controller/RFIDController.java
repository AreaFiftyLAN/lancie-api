package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.RFIDLinkDTO;
import ch.wisv.areafiftylan.exception.InvalidRFIDException;
import ch.wisv.areafiftylan.exception.RFIDNotFoundException;
import ch.wisv.areafiftylan.exception.RFIDTakenException;
import ch.wisv.areafiftylan.exception.TicketAlreadyLinkedException;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.relations.RFIDLink;
import ch.wisv.areafiftylan.service.RFIDService;
import ch.wisv.areafiftylan.service.TicketService;
import ch.wisv.areafiftylan.service.repository.RFIDLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

/**
 * Created by beer on 5-5-16.
 */

@RestController
@RequestMapping("/rfid")
@PreAuthorize("hasRole('ADMIN')")
public class RFIDController {
    @Autowired
    private RFIDService rfidService;

    @Autowired
    private TicketService ticketService;

    @RequestMapping(method = RequestMethod.GET)
    public Collection<RFIDLink> getRFIDLinks(){
        return rfidService.getAllRFIDLinks();
    }

    @RequestMapping(value = "/{rfid}/ticketId", method = RequestMethod.GET)
    public Long getTicketId(@PathVariable String rfid){
        return rfidService.getTicketIdByRFID(rfid);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> addRFIDLink(@RequestBody RFIDLinkDTO rfidLinkDTO){
        rfidService.addRFIDLink(rfidLinkDTO.getRfid(), rfidLinkDTO.getTicketId());

        return createResponseEntity(HttpStatus.OK, "Succesfully linked RFID with Ticket");
    }

    @RequestMapping(value = "/{rfid}", method = RequestMethod.DELETE)
    public RFIDLink removeRFIDLinkByRFID(@PathVariable String rfid){
        return rfidService.removeRFIDLink(rfid);
    }

    //Exceptions

    @ExceptionHandler(RFIDTakenException.class)
    public ResponseEntity<?> handleRFIDTakenException(RFIDTakenException e){
        return createResponseEntity(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(TicketAlreadyLinkedException.class)
    public ResponseEntity<?> handleInvalidRFIDException(TicketAlreadyLinkedException e){
        return createResponseEntity(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(InvalidRFIDException.class)
    public ResponseEntity<?> handleInvalidRFIDException(InvalidRFIDException e){
        return createResponseEntity(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
