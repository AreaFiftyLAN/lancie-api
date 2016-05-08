package ch.wisv.areafiftylan.exception;

/**
 * Created by beer on 8-5-16.
 */
public class RFIDTakenException extends RuntimeException {
    public RFIDTakenException(String rfid) {
        super("The RFID " + rfid + " has already been taken.");
    }
}
