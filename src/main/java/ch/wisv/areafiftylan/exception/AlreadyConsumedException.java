package ch.wisv.areafiftylan.exception;

/**
 * Created by beer on 16-5-16.
 */
public class AlreadyConsumedException extends RuntimeException {
    public AlreadyConsumedException(String consumption) {
        super("Consumption " + consumption + " has already been consumed.");
    }
}
