package ch.wisv.areafiftylan.exception;

import ch.wisv.areafiftylan.model.util.Consumption;

/**
 * Created by beer on 16-5-16.
 */
public class AlreadyConsumedException extends RuntimeException {
    public AlreadyConsumedException(Consumption consumption) {
        super("Consumption " + consumption.getName() + " has already been consumed.");
    }
}
