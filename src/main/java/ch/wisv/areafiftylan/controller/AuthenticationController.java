package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.exception.TokenNotFoundException;
import ch.wisv.areafiftylan.exception.UserNotFoundException;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.security.VerificationToken;
import ch.wisv.areafiftylan.service.MailService;
import ch.wisv.areafiftylan.service.UserService;
import ch.wisv.areafiftylan.service.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.UUID;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

@Controller
public class AuthenticationController {

    @Autowired
    MailService mailService;

    @Autowired
    UserService userService;

    @Autowired
    VerificationTokenRepository verificationTokenRepository;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ResponseEntity<?> getLoginPage() {
        return createResponseEntity(HttpStatus.UNAUTHORIZED, "Please log in");
    }

    @RequestMapping(value = "/user/resetPassword", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> resetPassword(HttpServletRequest request, @RequestParam("email") String userEmail) {

        User user = userService.getUserByEmail(userEmail).orElseThrow(() -> new UserNotFoundException(userEmail));

        String token = UUID.randomUUID().toString();
        //        userService.createPasswordResetTokenForUser(user, token);
        String url = request.getContextPath() + "/registrationConfirm" + token;
        // send mail with URL

        //        return new GenericResponse(messages.getMessage("message.resetPasswordEmail", null, request
        // .getLocale()));
        return null;
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
