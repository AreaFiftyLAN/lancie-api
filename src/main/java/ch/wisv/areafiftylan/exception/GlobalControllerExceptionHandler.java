package ch.wisv.areafiftylan.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

/**
 * This class handles exceptions for ALL Controllers. Whenever a Controller throws an exception that is listed here, the
 * corresponding method will be used to handle the exception.
 */
@ControllerAdvice
class GlobalControllerExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        return createResponseEntity(HttpStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return createResponseEntity(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException ex) {
        return createResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<?> handleOrderNotFoundException(OrderNotFoundException ex) {
        return createResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<?> handleTokenNotFoundException(TokenNotFoundException ex) {
        return createResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<?> handleInvalidTokenException(InvalidTokenException ex) {
        return createResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<?> handleTicketNotFoundException(TicketNotFoundException ex) {
        return createResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RFIDNotFoundException.class)
    public ResponseEntity<?> handleRFIDNotFoundException(RFIDNotFoundException e){
        return createResponseEntity(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
