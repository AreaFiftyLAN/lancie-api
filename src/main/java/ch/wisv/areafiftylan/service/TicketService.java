package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.exception.TicketUnavailableException;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.security.token.TicketTransferToken;

import java.util.Collection;

public interface TicketService {
    Ticket getTicketById(Long ticketId);

    Ticket removeTicket(Long ticketId);

    Integer getNumberSoldOfType(TicketType type);

    Collection<Ticket> findValidTicketsByOwnerUsername(String username);

    Collection<Ticket> getAllTickets();

    /**
     * Mark the ticket with the given Id as valid.
     *
     * @param ticketId The ID of the ticket to be marked as valid
     */
    void validateTicket(Long ticketId);

    /**
     * Check if a ticket is available, and return when it is. When a ticket is unavailable (sold out for instance) a
     * TicketUnavailableException is thrown
     *
     * @param type          Type of the Ticket requested
     * @param owner         User that wants the Ticket
     * @param pickupService If the Ticket includes the pickupService
     *
     * @return The requested ticket, if available
     *
     * @throws TicketUnavailableException If the requested ticket is sold out.
     */
    Ticket requestTicketOfType(TicketType type, User owner, boolean pickupService, boolean chMember);

    /**
     * Sets up the ticket for transfer
     * @param ticketId The ID of the ticket to be transferred
     * @param goalUserName The name of the user which should receive the ticket
     */
    TicketTransferToken setupForTransfer(Long ticketId, String goalUserName);

    /**
     * Transfer the ticket to another user
     *
     * @param token The token that refers to the TicketTransferToken
     */
    void transferTicket(String token);

    /**
     * Cancel a ticket transfer
     *
     * @param token The token that refers to the TicketTransferToken
     */
    void cancelTicketTransfer(String token);

    /**
     * Get all tickets over which a user has control. His own, as well as his teammates their tickets if the user is captain.
     *
     * @param user The user whose tickets are requested
     * @return All tickets over which a user has control.
     */
    Collection<Ticket>getTicketsFromTeamMembers(User user);

    Collection<TicketTransferToken> getValidTicketTransferTokensByUser(String username);
}
