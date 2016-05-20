package ch.wisv.areafiftylan.exception;

import ch.wisv.areafiftylan.model.util.Consumption;

/**
 * Created by beer on 8-5-16.
 */
public class ConsumptionNotSupportedException extends RuntimeException {
    public ConsumptionNotSupportedException(Consumption consumptionType) {
        super("Consumption type \"" + consumptionType.getName() + "\" is unsupported.");
    }
}
