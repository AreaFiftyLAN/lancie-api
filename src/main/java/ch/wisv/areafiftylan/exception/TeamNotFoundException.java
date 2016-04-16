package ch.wisv.areafiftylan.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class TeamNotFoundException extends RuntimeException {

    public TeamNotFoundException(String teamname) {
        super("could not find team '" + teamname + "'.");
    }

    public TeamNotFoundException(Long teamId) {
        super("Could not find team with id " + teamId);
    }

}
