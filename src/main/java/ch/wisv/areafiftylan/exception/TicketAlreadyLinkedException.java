package ch.wisv.areafiftylan.exception;

/**
 * Created by beer on 8-5-16.
 */
public class TicketAlreadyLinkedException extends RuntimeException {
    public TicketAlreadyLinkedException() {
        super("Ticket has already been linked to a RFID");
    }
}
