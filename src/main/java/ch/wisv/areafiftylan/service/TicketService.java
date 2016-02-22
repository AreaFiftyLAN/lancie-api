package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.exception.TicketUnavailableException;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.TicketType;
import org.springframework.stereotype.Service;

import java.util.Optional;

public interface TicketService {
    Optional<Ticket> getTicketByKey(String key);

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
     * Transfer the ticket to another user
     *
     * @param user      The user to transfer the ticket to
     * @param ticketKey The key of the ticket to be transferred
     */
    void transferTicket(User user, String ticketKey);
}
