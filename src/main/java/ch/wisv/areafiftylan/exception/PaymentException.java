package ch.wisv.areafiftylan.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by sille on 26-12-15.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PaymentException extends RuntimeException {

    public PaymentException(String message) {
        super(message);
    }
}
