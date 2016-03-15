package ch.wisv.areafiftylan.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TeamNotFoundException extends RuntimeException {

    public TeamNotFoundException(String teamname) {
        super("could not find team '" + teamname + "'.");
    }

    public TeamNotFoundException(String m) {
        super(m);
    }
}
