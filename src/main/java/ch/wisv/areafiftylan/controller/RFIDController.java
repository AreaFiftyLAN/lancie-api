package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.RFIDLinkDTO;
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

    @RequestMapping(value = "/{rfid}/ticket", method = RequestMethod.GET)
    public Ticket getRFIDLink(@PathVariable String rfid){
        return rfidService.getTicketByRFID(rfid);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> addRFIDLink(@RequestBody RFIDLinkDTO rfidLinkDTO){
        if(rfidService.isRFIDUsed(rfidLinkDTO.getRfid())){
            return createResponseEntity(HttpStatus.CONFLICT, "RFID " + rfidLinkDTO.getRfid() + " is already in use");
        }

        Ticket t = ticketService.getTicketById(rfidLinkDTO.getTicketId());

        RFIDLink newLink = new RFIDLink(rfidLinkDTO.getRfid(), t);
        rfidService.addRFIDLink(newLink);

        return createResponseEntity(HttpStatus.OK, "Succesfully linked RFID with Ticket");
    }
}
