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

import ch.wisv.areafiftylan.exception.TicketTransferTokenException;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.model.TicketInformationResponse;
import ch.wisv.areafiftylan.products.model.TicketOption;
import ch.wisv.areafiftylan.products.model.TicketType;
import ch.wisv.areafiftylan.products.service.OrderService;
import ch.wisv.areafiftylan.products.service.TicketService;
import ch.wisv.areafiftylan.security.token.TicketTransferToken;
import ch.wisv.areafiftylan.users.model.User;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import net.logstash.logback.marker.Markers;
import org.slf4j.Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping(value = "/tickets")
@Slf4j
public class TicketRestController {
    private final TicketService ticketService;
    private final OrderService orderService;

    private Marker controllerMarker = Markers.append("controller", "tickets");

    @Autowired
    public TicketRestController(TicketService ticketService, OrderService orderService) {
        this.ticketService = ticketService;
        this.orderService = orderService;
    }

    @PreAuthorize("@currentUserServiceImpl.isTicketOwner(principal, #ticketId)")
    @PostMapping("/transfer/{ticketId}")
    public ResponseEntity<?> requestTicketTransfer(@PathVariable Long ticketId, @RequestBody String goalEmail) {
        TicketTransferToken ttt = ticketService.setupForTransfer(ticketId, goalEmail);

        log.info(controllerMarker, "Ticket {} set up for transfer to {}", ticketId, goalEmail,
                StructuredArguments.v("ticket_id", ticketId));

        return createResponseEntity(HttpStatus.OK, "Ticket successfully set up for transfer.", ttt.getToken());
    }

    @PreAuthorize("@currentUserServiceImpl.isTicketReceiver(principal, #token)")
    @PutMapping("/transfer")
    public ResponseEntity<?> transferTicket(@RequestBody String token, @AuthenticationPrincipal User user) {
        Ticket ticket = ticketService.transferTicket(token);

        log.info(controllerMarker, "Ticket {} transferred to {}", ticket.getId(), user.getEmail(),
                StructuredArguments.v("ticket_id", ticket.getId()),
                StructuredArguments.v("ticket_type", ticket.getType()));

        return createResponseEntity(HttpStatus.OK, "Ticket successfully transferred.");
    }

    @PreAuthorize("@currentUserServiceImpl.isTicketSender(principal, #token)")
    @DeleteMapping("/transfer")
    public ResponseEntity<?> cancelTicketTransfer(@RequestBody String token) {
        Ticket ticket = ticketService.cancelTicketTransfer(token);

        log.info(controllerMarker, "Ticket {} transfer cancelled", ticket.getId(),
                StructuredArguments.v("ticket_id", ticket.getId()));

        return createResponseEntity(HttpStatus.OK, "Ticket transfer successfully cancelled.");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/tokens")
    public ResponseEntity<?> getTicketTokensOpenForTransfer(@AuthenticationPrincipal User user) {
        Collection<TicketTransferToken> tokens = ticketService.getValidTicketTransferTokensByUserEmail(user.getEmail());

        return createResponseEntity(HttpStatus.OK, "Ticket transfer tokens successfully retrieved.", tokens);
    }

    /**
     * This method returns an overview of available tickets with some additional information
     *
     * @return A collection of all TicketTypes and their availability
     */
    @GetMapping("/available")
    public Collection<TicketInformationResponse> getAvailableTickets() {
        return orderService.getAvailableTickets();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/teammembers")
    public Collection<Ticket> getTicketsFromTeamMembers(@AuthenticationPrincipal User user) {
        return ticketService.getOwnedTicketsAndFromTeamMembers(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Collection<Ticket> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/transport")
    public Collection<Ticket> getAllTicketsWithTransport() {
        return ticketService.getAllTicketsWithTransport();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/types")
    public ResponseEntity<?> addTicketType(@RequestBody @Validated TicketType type) {
        TicketType ticketType = ticketService.addTicketType(type);
        return createResponseEntity(HttpStatus.CREATED, "TicketType successfully added", ticketType);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/types")
    public Collection<TicketType> readTicketTypes() {
        return ticketService.getAllTicketTypes();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/types/{typeId}")
    public ResponseEntity<?> updateTicketType(@PathVariable Long typeId, @RequestBody @Validated TicketType type) {
        TicketType ticketType = ticketService.updateTicketType(typeId, type);
        return createResponseEntity(HttpStatus.OK, "TicketType successfully updated.", ticketType);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/types/{typeId}")
    public ResponseEntity<?> deleteTicketType(@PathVariable Long typeId) {
        ticketService.deleteTicketType(typeId);
        return createResponseEntity(HttpStatus.OK, "TicketType successfully deleted.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/options")
    public ResponseEntity<?> addTicketOption(@RequestBody @Validated TicketOption option) {

        TicketOption ticketOption = ticketService.addTicketOption(option);

        return createResponseEntity(HttpStatus.CREATED, "TicketOption successfully added", ticketOption);
    }

    @ExceptionHandler(TicketTransferTokenException.class)
    public ResponseEntity<?> handleDuplicateTicketTransFerException(TicketTransferTokenException ex) {
        return createResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
