package ch.wisv.areafiftylan.exception;

/**
 * Created by sille on 26-12-15.
 */
public class PaymentException extends RuntimeException {
    public PaymentException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public PaymentException(String message) {
        super(message);
    }
}
