package ch.wisv.areafiftylan.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TicketNotTransferrableException extends RuntimeException{
    public TicketNotTransferrableException(String ticketKey) {
        super("Ticket with key " + ticketKey + " is not found or not transferrable.");
    }
}
