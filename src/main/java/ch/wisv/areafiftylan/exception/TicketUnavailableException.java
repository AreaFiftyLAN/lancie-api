package ch.wisv.areafiftylan.exception;

import ch.wisv.areafiftylan.model.util.TicketType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by sille on 22-12-15.
 */

public class TicketUnavailableException extends RuntimeException {

    public TicketUnavailableException(TicketType type) {
        super("Ticket with type " + type.toString() + " has been sold out");
    }


}
