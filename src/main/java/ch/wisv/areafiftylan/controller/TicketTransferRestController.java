package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.TransferDTO;
import ch.wisv.areafiftylan.exception.NotGoalUserException;
import ch.wisv.areafiftylan.exception.TicketNotFoundException;
import ch.wisv.areafiftylan.exception.TicketNotTransferrableException;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.OrderService;
import ch.wisv.areafiftylan.service.TicketService;
import ch.wisv.areafiftylan.service.UserService;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import ch.wisv.areafiftylan.util.ResponseEntityBuilder;
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
    private UserService userService;
    private TicketRepository ticketRepository;

    @Autowired
    public TicketTransferRestController(TicketService ticketService, UserService userService, TicketRepository ticketRepository) {
        this.ticketService = ticketService;
        this.userService = userService;
        this.ticketRepository = ticketRepository;
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/tickets/transfer", method = RequestMethod.POST)
    public ResponseEntity<?> requestTicketTransfer(Authentication auth, @RequestBody @Validated TransferDTO transferDTO){
        String username = transferDTO.getGoalUsername();
        String ticketKey = transferDTO.getTicketKey();

        User sender = (User)auth.getPrincipal();

        User u = userService.getUserByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found."));
        Ticket t = ticketRepository.findByKey(ticketKey).orElseThrow(() -> new TicketNotFoundException(ticketKey));

        if(!t.getOwner().equals(sender)) return createResponseEntity(HttpStatus.FORBIDDEN, "You are not allowed to set this ticket up for transfer");

        ticketService.setupForTransfer(t, u);

        return createResponseEntity(HttpStatus.OK, "Ticket succesfully set up for transfer");
    }

    @RequestMapping(value = "/tickets/transfer/{ticketKey}", method = RequestMethod.PUT)
    public ResponseEntity<?> transferTicket(Authentication auth, @PathVariable String ticketKey){
        User u = (User)auth.getPrincipal();

        //Here an TicketNotTransferrable is thrown instead of ticket not found, otherwise there is an information leak since they know when a ticket doesn't exist or is just not transferrable
        Ticket t = ticketRepository.findByKey(ticketKey).orElseThrow(() -> new TicketNotTransferrableException(ticketKey));

        ticketService.transferTicket(u, t);

        return createResponseEntity(HttpStatus.OK, "Ticket succesfully transferred");
    }

}
