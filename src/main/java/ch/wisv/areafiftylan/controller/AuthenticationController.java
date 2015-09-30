package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.util.ResponseEntityBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AuthenticationController {

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ResponseEntity<?> getLoginPage() {
        return ResponseEntityBuilder.createResponseEntity(HttpStatus.UNAUTHORIZED, "Please log in");
    }

}
