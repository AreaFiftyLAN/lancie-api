package ch.wisv.areafiftylan.exception;

/**
 * Created by beer on 5-5-16.
 */
public class RFIDNotFoundException extends RuntimeException {
    public RFIDNotFoundException() {
        super("Failed to find RFID");
    }
}
