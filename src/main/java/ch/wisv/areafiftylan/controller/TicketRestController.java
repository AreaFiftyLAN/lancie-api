package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.TicketInformationResponse;
import ch.wisv.areafiftylan.exception.TicketUnavailableException;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.exception.DuplicateTicketTransferTokenException;
import ch.wisv.areafiftylan.security.token.TicketTransferToken;
import ch.wisv.areafiftylan.service.OrderService;
import ch.wisv.areafiftylan.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

@RestController
public class TicketRestController {
    private TicketService ticketService;
    private OrderService orderService;

    @Autowired
    public TicketRestController(TicketService ticketService, OrderService orderService) {
        this.ticketService = ticketService;
        this.orderService = orderService;
    }

    @PreAuthorize("@currentUserServiceImpl.isTicketOwner(principal, #ticketId)")
    @RequestMapping(value = "/tickets/transfer/{ticketId}", method = RequestMethod.POST)
    public ResponseEntity<?> requestTicketTransfer(@PathVariable Long ticketId, @RequestBody String goalUsername){
        TicketTransferToken ttt = ticketService.setupForTransfer(ticketId, goalUsername);

        return createResponseEntity(HttpStatus.OK, "Ticket successfully set up for transfer.", ttt);
    }

    @PreAuthorize("@currentUserServiceImpl.isTicketReceiver(principal, #token)")
    @RequestMapping(value = "/tickets/transfer", method = RequestMethod.PUT)
    public ResponseEntity<?> transferTicket(@RequestBody String token){
        ticketService.transferTicket(token);

        return createResponseEntity(HttpStatus.OK, "Ticket successfully transferred.");
    }

    @PreAuthorize("@currentUserServiceImpl.isTicketSender(principal, #token)")
    @RequestMapping(value = "/tickets/transfer", method = RequestMethod.DELETE)
    public ResponseEntity<?> cancelTicketTransfer(@RequestBody String token){
        ticketService.cancelTicketTransfer(token);

        return createResponseEntity(HttpStatus.OK, "Ticket transfer successfully cancelled.");
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/tickets/tokens", method = RequestMethod.GET)
    public ResponseEntity<?> getTicketTokensOpenForTransfer(Authentication auth) {
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        Collection<TicketTransferToken> tokens = ticketService.getValidTicketTransferTokensByUser(currentUser.getUsername());

        return createResponseEntity(HttpStatus.OK, "Ticket transfer tokens successfully retrieved.", tokens);
    }

    @ExceptionHandler(DuplicateTicketTransferTokenException.class)
    public ResponseEntity<?> handleDuplicateTicketTransFerException(DuplicateTicketTransferTokenException ex) {
        return createResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * This method returns an overview of available tickets with some additional information
     *
     * @return A collection of all TicketTypes and their availability
     */
    @RequestMapping(value = "/tickets/available", method = RequestMethod.GET)
    public Collection<TicketInformationResponse> getAvailableTickets() {
        return orderService.getAvailableTickets();
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/tickets/incontrol", method = RequestMethod.GET)
    public Collection<Ticket> getTicketsInControl(Authentication auth) {
        User u = (User)auth.getPrincipal();

        return ticketService.getTicketsInControl(u);
    }
}
