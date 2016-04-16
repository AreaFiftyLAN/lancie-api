package ch.wisv.areafiftylan.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by sille on 27-12-15.
 */

public class PaymentServiceConnectionException extends RuntimeException {
    public PaymentServiceConnectionException(String s) {
        super(s);
    }
}
