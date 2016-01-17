package ch.wisv.areafiftylan.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by sille on 13-1-16.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(String message) {
        super(message);
    }
}
