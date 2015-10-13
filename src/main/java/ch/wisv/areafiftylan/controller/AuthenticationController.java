package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.exception.TokenNotFoundException;
import ch.wisv.areafiftylan.exception.UserNotFoundException;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.security.PasswordResetToken;
import ch.wisv.areafiftylan.security.VerificationToken;
import ch.wisv.areafiftylan.service.MailService;
import ch.wisv.areafiftylan.service.UserService;
import ch.wisv.areafiftylan.service.repository.PasswordResetTokenRepository;
import ch.wisv.areafiftylan.service.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Map;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

@Controller
public class AuthenticationController {

    @Autowired
    MailService mailService;

    @Autowired
    UserService userService;

    @Autowired
    VerificationTokenRepository verificationTokenRepository;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ResponseEntity<?> getLoginPage() {
        return createResponseEntity(HttpStatus.UNAUTHORIZED, "Please log in");
    }

    @RequestMapping(value = "/requestResetPassword", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> requestResetPassword(HttpServletRequest request, @RequestBody Map<String, String> body) {
        String email = body.get("email");

        User user = userService.getUserByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        userService.requestResetPassword(user, request);
        return createResponseEntity(HttpStatus.OK, "Password reset link sent to " + email);
    }

    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String password = body.get("password");

        PasswordResetToken passwordResetToken =
                passwordResetTokenRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
        User user = passwordResetToken.getUser();

        userService.resetPassword(user.getId(), password);
        return createResponseEntity(HttpStatus.OK, "Password set!");
    }

    @RequestMapping(value = "/confirmRegistration", method = RequestMethod.GET)
    public ResponseEntity<?> confirmRegistration(@RequestParam("token") String token) throws Exception {

        VerificationToken verificationToken =
                verificationTokenRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));

        User user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            return createResponseEntity(HttpStatus.UNAUTHORIZED, "Token expired");
        }

        user.setEnabled(true);
        userService.save(user);
        return createResponseEntity(HttpStatus.OK, "Succesfully verified");
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<?> handleAccessDeniedException(TokenNotFoundException ex) {
        return createResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }

}
