package ch.wisv.areafiftylan.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class TokenNotFoundException extends RuntimeException {

    public TokenNotFoundException(String token) {
        super("could not find token '" + token + "'.");
    }
}
