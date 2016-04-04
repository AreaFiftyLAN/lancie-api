package ch.wisv.areafiftylan.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotGoalUserException extends RuntimeException{
    public NotGoalUserException(){
        super("You are not the goal user of this operation");
    }
}
