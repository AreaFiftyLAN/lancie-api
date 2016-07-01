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

package ch.wisv.areafiftylan.products.controller;

import ch.wisv.areafiftylan.products.model.TicketInformationResponse;
import ch.wisv.areafiftylan.exception.DuplicateTicketTransferTokenException;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.security.token.TicketTransferToken;
import ch.wisv.areafiftylan.products.service.OrderService;
import ch.wisv.areafiftylan.products.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping(value = "/tickets")
public class TicketRestController {
    private TicketService ticketService;
    private OrderService orderService;

    @Autowired
    public TicketRestController(TicketService ticketService, OrderService orderService) {
        this.ticketService = ticketService;
        this.orderService = orderService;
    }

    @PreAuthorize("@currentUserServiceImpl.isTicketOwner(principal, #ticketId)")
    @RequestMapping(value = "/transfer/{ticketId}", method = RequestMethod.POST)
    public ResponseEntity<?> requestTicketTransfer(@PathVariable Long ticketId, @RequestBody String goalUsername) {
        TicketTransferToken ttt = ticketService.setupForTransfer(ticketId, goalUsername);

        return createResponseEntity(HttpStatus.OK, "Ticket successfully set up for transfer.", ttt.getToken());
    }

    @PreAuthorize("@currentUserServiceImpl.isTicketReceiver(principal, #token)")
    @RequestMapping(value = "/transfer", method = RequestMethod.PUT)
    public ResponseEntity<?> transferTicket(@RequestBody String token) {
        ticketService.transferTicket(token);

        return createResponseEntity(HttpStatus.OK, "Ticket successfully transferred.");
    }

    @PreAuthorize("@currentUserServiceImpl.isTicketSender(principal, #token)")
    @RequestMapping(value = "/transfer", method = RequestMethod.DELETE)
    public ResponseEntity<?> cancelTicketTransfer(@RequestBody String token) {
        ticketService.cancelTicketTransfer(token);

        return createResponseEntity(HttpStatus.OK, "Ticket transfer successfully cancelled.");
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/tokens", method = RequestMethod.GET)
    public ResponseEntity<?> getTicketTokensOpenForTransfer(Authentication auth) {
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        Collection<TicketTransferToken> tokens =
                ticketService.getValidTicketTransferTokensByUser(currentUser.getUsername());

        return createResponseEntity(HttpStatus.OK, "Ticket transfer tokens successfully retrieved.", tokens);
    }

    /**
     * This method returns an overview of available tickets with some additional information
     *
     * @return A collection of all TicketTypes and their availability
     */
    @RequestMapping(value = "/available", method = RequestMethod.GET)
    public Collection<TicketInformationResponse> getAvailableTickets() {
        return orderService.getAvailableTickets();
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/teammembers", method = RequestMethod.GET)
    public Collection<Ticket> getTicketsFromTeamMembers(Authentication auth) {
        User u = (User) auth.getPrincipal();

        return ticketService.getOwnedTicketsAndFromTeamMembers(u);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(method = RequestMethod.GET)
    public Collection<Ticket> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/transport", method = RequestMethod.GET)
    public Collection<Ticket> getAllTicketsWithTransport() {
        return ticketService.getAllTicketsWithTransport();
    }

    @ExceptionHandler(DuplicateTicketTransferTokenException.class)
    public ResponseEntity<?> handleDuplicateTicketTransFerException(DuplicateTicketTransferTokenException ex) {
        return createResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
