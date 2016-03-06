package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.TransferDTO;
import ch.wisv.areafiftylan.exception.TicketNotFoundException;
import ch.wisv.areafiftylan.exception.TicketNotTransferrableException;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.TicketService;
import ch.wisv.areafiftylan.service.UserService;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

@RestController
public class TicketTransferRestController {
    private TicketService ticketService;

    @Autowired
    public TicketTransferRestController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PreAuthorize("@currentUserServiceImpl.isTicketOwner(principal, #ticketKey)")
    @RequestMapping(value = "/tickets/transfer/{ticketKey}", method = RequestMethod.POST)
    public ResponseEntity<?> requestTicketTransfer(@PathVariable String ticketKey, @RequestBody @Validated TransferDTO transferDTO){
        String username = transferDTO.getGoalUsername();

        ticketService.setupForTransfer(ticketKey, username);

        return createResponseEntity(HttpStatus.OK, "Ticket successfully set up for transfer");
    }

    @PreAuthorize("@currentUserServiceImpl.isTicketReceiver(principal, #ticketKey)")
    @RequestMapping(value = "/tickets/transfer/{ticketKey}", method = RequestMethod.PUT)
    public ResponseEntity<?> transferTicket(@PathVariable String ticketKey){
        ticketService.transferTicket(ticketKey);

        return createResponseEntity(HttpStatus.OK, "Ticket successfully transferred");
    }

    @PreAuthorize("@currentUserServiceImpl.isTicketOwner(principal, #ticketKey)")
    @RequestMapping(value = "/tickets/transfer/{ticketKey}", method = RequestMethod.DELETE)
    public ResponseEntity<?> cancelTicketTransfer(@PathVariable String ticketKey){
        ticketService.cancelTicketTransfer(ticketKey);

        return createResponseEntity(HttpStatus.OK, "Ticket transfer successfully cancelled");
    }
}
