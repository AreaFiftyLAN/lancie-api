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

package ch.wisv.areafiftylan.extras.rfid.controller;

import ch.wisv.areafiftylan.exception.InvalidRFIDException;
import ch.wisv.areafiftylan.exception.RFIDNotFoundException;
import ch.wisv.areafiftylan.exception.RFIDTakenException;
import ch.wisv.areafiftylan.extras.rfid.model.RFIDLink;
import ch.wisv.areafiftylan.extras.rfid.model.RFIDLinkDTO;
import ch.wisv.areafiftylan.extras.rfid.service.RFIDService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("/rfid")
@PreAuthorize("hasRole('ADMIN')")
public class RFIDController {

    private final RFIDService rfidService;

    @Autowired
    public RFIDController(RFIDService rfidService) {
        this.rfidService = rfidService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Collection<RFIDLink> getRFIDLinks() {
        return rfidService.getAllRFIDLinks();
    }

    @RequestMapping(value = "/{rfid}/ticketId", method = RequestMethod.GET)
    public Long getTicketId(@PathVariable String rfid) {
        return rfidService.getTicketIdByRFID(rfid);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> addRFIDLink(@RequestBody RFIDLinkDTO rfidLinkDTO) {
        rfidService.addRFIDLink(rfidLinkDTO.getRfid(), rfidLinkDTO.getTicketId());

        return createResponseEntity(HttpStatus.OK, "Succesfully linked RFID with Ticket");
    }

    @RequestMapping(value = "/{rfid}", method = RequestMethod.DELETE)
    public RFIDLink removeRFIDLinkByRFID(@PathVariable String rfid) {
        return rfidService.removeRFIDLink(rfid);
    }

    @RequestMapping(value = "/tickets/{ticketId}", method = RequestMethod.DELETE)
    public RFIDLink deleteRFIDByTicketId(@PathVariable Long ticketId) {
        return rfidService.removeRFIDLink(ticketId);
    }

    //Exceptions

    @ExceptionHandler(RFIDTakenException.class)
    public ResponseEntity<?> handleRFIDTakenException(RFIDTakenException e) {
        return createResponseEntity(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(InvalidRFIDException.class)
    public ResponseEntity<?> handleInvalidRFIDException(InvalidRFIDException e) {
        return createResponseEntity(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(RFIDNotFoundException.class)
    public ResponseEntity<?> handleRFIDNotFoundException(RFIDNotFoundException e) {
        return createResponseEntity(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
