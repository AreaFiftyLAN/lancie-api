package ch.wisv.areafiftylan.exception;

/**
 * Created by beer on 8-5-16.
 */
public class ConsumptionNotSupportedException extends RuntimeException {
    public ConsumptionNotSupportedException(String consumptionType) {
        super("Consumption type \"" + consumptionType + "\" is unsupported.");
    }
}
