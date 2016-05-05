package ch.wisv.areafiftylan.exception;

/**
 * Created by beer on 5-5-16.
 */
public class InvalidRFIDException extends RuntimeException {
    public InvalidRFIDException(String invalidRFID) {
        super(invalidRFID + " is an invald RFID.");
    }
}
