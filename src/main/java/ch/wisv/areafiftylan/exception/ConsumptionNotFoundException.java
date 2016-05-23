package ch.wisv.areafiftylan.exception;

import ch.wisv.areafiftylan.model.util.Consumption;

/**
 * Created by beer on 8-5-16.
 */
public class ConsumptionNotFoundException extends RuntimeException {
    public ConsumptionNotFoundException(Long consumptionId) {
        super("Can't find a consumption with id: \"" + consumptionId + "\" is unsupported.");
    }
}
