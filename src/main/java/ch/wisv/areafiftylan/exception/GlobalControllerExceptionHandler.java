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
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return createResponseEntity(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRunTimeException(RuntimeException ex) {
        return createResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception ex) {
        ex.printStackTrace();
        return createResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong. Please contact the Administrators.");
    }
}
