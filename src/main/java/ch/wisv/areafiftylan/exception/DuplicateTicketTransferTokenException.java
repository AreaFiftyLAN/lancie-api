package ch.wisv.areafiftylan.exception;

/**
 * Created by martijn on 23-4-16.
 */
public class DuplicateTicketTransferTokenException extends RuntimeException {
    public DuplicateTicketTransferTokenException(Long ticketId) {
        super("Ticket " + ticketId + " is already set up for transfer!");
    }
}
