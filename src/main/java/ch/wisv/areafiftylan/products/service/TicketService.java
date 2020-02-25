/*
 * Copyright (c) 2018  W.I.S.V. 'Christiaan Huygens'
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

package ch.wisv.areafiftylan.products.service;

import ch.wisv.areafiftylan.exception.TicketUnavailableException;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.model.TicketOption;
import ch.wisv.areafiftylan.products.model.TicketType;
import ch.wisv.areafiftylan.security.token.TicketTransferToken;
import ch.wisv.areafiftylan.users.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TicketService {
    Ticket getTicketById(Long ticketId);

    Ticket removeTicket(Long ticketId);

    Integer getNumberSoldOfType(TicketType type);

    Collection<Ticket> findValidTicketsByOwnerEmail(String email);

    Collection<Ticket> getAllTickets();

    /**
     * Mark the ticket with the given Id as valid.
     *
     * @param ticketId The ID of the ticket to be marked as valid
     */
    void validateTicket(Long ticketId);

    TicketOption getTicketOptionByName(String name);

    /**
     * Check if a ticket is available, and return when it is. When a ticket is unavailable (sold out for instance) a
     * TicketUnavailableException is thrown
     *
     * @param type          Type of the Ticket requested
     * @return The requested ticket, if available
     *
     * @throws TicketUnavailableException If the requested ticket is sold out.
     */
    default Ticket requestTicketOfType(String type, List<String> options) {
        return requestTicketOfType(null, type, options);
    }

    Ticket requestTicketOfType(User user, String type, List<String> options);

    Ticket requestTicketOfType(User user, TicketType type, List<TicketOption> options);

    /**
     * Sets up the ticket for transfer
     *
     * @param ticketId      The ID of the ticket to be transferred
     * @param receiverEmail    The name of the user which should receive the ticket
     */
    TicketTransferToken setupForTransfer(Long ticketId, String receiverEmail);

    /**
     * Transfer the ticket to another user
     *
     * @param token The token that refers to the TicketTransferToken
     */
    Ticket transferTicket(String token);

    /**
     * Cancel a ticket transfer
     *
     * @param token The token that refers to the TicketTransferToken
     */
    Ticket cancelTicketTransfer(String token);

    /**
     * Get all tickets over which a user has control. His own, as well as his teammates their tickets if the user is
     * captain.
     *
     * @param user The user whose tickets are requested
     *
     * @return All tickets over which a user has control.
     */
    Collection<Ticket> getOwnedTicketsAndFromTeamMembers(User user);

    Collection<TicketTransferToken> getValidTicketTransferTokensByUserEmail(String email);

    Collection<Ticket> getAllTicketsWithTransport(String ticketOption);

    Ticket assignTicketToUser(Long TicketId, String email);

    TicketType addTicketType(TicketType type);

    Collection<TicketType> getAllTicketTypes();

    TicketType updateTicketType(Long typeId, TicketType type);

    TicketType getTicketTypeById(Long typeId);

    void deleteTicketType(Long typeId);

    TicketOption addTicketOption(TicketOption option);

    void deleteTicketOption(Long optionID);

    Collection<TicketOption> getAllTicketOptions();
}
