package ch.wisv.areafiftylan.exception;

public class EventException extends RuntimeException {

    public EventException(Long eventId) {
        super("Could not find event with id " + eventId);
    }

    public EventException(String message) {
        super(message);
    }
}