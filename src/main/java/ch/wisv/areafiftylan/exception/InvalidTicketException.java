package ch.wisv.areafiftylan.exception;

/**
 * Created by Sille Kamoen on 22-5-2016.
 */
public class InvalidTicketException extends RuntimeException {
    public InvalidTicketException(String message) {
        super(message);
    }
}
