/*
 * Copyright (c) 2016  W.I.S.V. 'Christiaan Huygens'
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.wisv.areafiftylan.security.authentication;

import ch.wisv.areafiftylan.exception.InvalidTokenException;
import ch.wisv.areafiftylan.exception.TokenNotFoundException;
import ch.wisv.areafiftylan.products.service.OrderService;
import ch.wisv.areafiftylan.security.token.PasswordResetToken;
import ch.wisv.areafiftylan.security.token.VerificationToken;
import ch.wisv.areafiftylan.security.token.repository.PasswordResetTokenRepository;
import ch.wisv.areafiftylan.security.token.repository.VerificationTokenRepository;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.model.UserDTO;
import ch.wisv.areafiftylan.users.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;


/**
 * This class handles all authentication related requests. These requests deal with the login message, the user email
 * verification and the password resets. Due to the minimal logic involved with tokens, it interacts directly with their
 * respective repositories.
 */
@Controller
@Log4j2
public class AuthenticationController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final OrderService orderService;

    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    public AuthenticationController(UserService userService, AuthenticationService authenticationService,
                                    OrderService orderService, VerificationTokenRepository verificationTokenRepository,
                                    PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userService = userService;
        this.authenticationService = authenticationService;
        this.orderService = orderService;

        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    /**
     * This basic GET method for the /login endpoint returns a simple login form.
     *
     * @return Login view
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView getLoginPage() {
        return new ModelAndView("loginForm");
    }

    @RequestMapping(value = "/token", method = RequestMethod.GET)
    public ResponseEntity<?> checkSession() {
        return createResponseEntity(HttpStatus.OK, "Here's your token!");
    }

    @RequestMapping(value = "/token", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody UserDTO userDTO) {
        String authToken = authenticationService.createNewAuthToken(userDTO.getUsername(), userDTO.getPassword());

        return createResponseEntity(HttpStatus.OK, "Token successfully created", authToken);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/token/verify", method = RequestMethod.GET)
    public ResponseEntity<?> verifyToken() {
        return createResponseEntity(HttpStatus.OK, "Token is valid!");
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public ResponseEntity<?> removeSession(@RequestHeader("X-Auth-Token") String xAuth) {
        authenticationService.removeAuthToken(xAuth);

        return createResponseEntity(HttpStatus.OK, "Successfully logged out");
    }


    /**
     * This method requests a passwordResetToken and sends it to the user. With this token, the user can reset his
     * password.
     *
     * @param request The HttpServletRequest of the call
     * @param body    The body, should only contain an email parameter TODO: This can be done more elegantly
     *
     * @return A status message telling whether the action was successful
     */
    @RequestMapping(value = "/requestResetPassword", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> requestResetPassword(HttpServletRequest request, @RequestBody Map<String, String> body) {
        String username = body.get("username");

        log.log(Level.getLevel("A5L"), "Requesting password reset on email {}.", username);

        User user = userService.getUserByUsername(username);

        userService.requestResetPassword(user, request);

        log.log(Level.getLevel("A5L"), "Successfully requested password reset on email {}.", username);

        return createResponseEntity(HttpStatus.OK, "Password reset link sent to " + username);
    }

    /**
     * After requesting a token, the user can reset his password with a POST call to this method. It should contain the
     * token and a password. The user is derived from the token.
     *
     * @param body The body, containing a token and password parameter. TODO: This should be validated
     *
     * @return A status message telling whethere the action was successful
     */
    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) throws InvalidTokenException {
        String token = body.get("token");
        String password = body.get("password");

        PasswordResetToken passwordResetToken =
                passwordResetTokenRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));

        //Check validity of the token
        if (!passwordResetToken.isValid()) {
            throw new InvalidTokenException();
        }

        // Get the user associated to the token
        User user = passwordResetToken.getUser();
        userService.resetPassword(user.getId(), password);

        // Disable the token for future use.
        passwordResetToken.use();
        passwordResetTokenRepository.saveAndFlush(passwordResetToken);

        return createResponseEntity(HttpStatus.OK, "Password set!");
    }

    /**
     * This function confirms the registration of a user based on the provided token. If retrieves the token from the
     * repository. From this token, we get the user. For this user, the enabled flag is set to true. After this, the
     * token is marked as used so it can not be used again.
     * <p>
     * TODO: Token logic could be moved to a dedicated service
     *
     * @param token The token as generated for the user opon registration
     *
     * @return A status message containing information about the operation
     *
     * @throws TokenNotFoundException if the token can't be found
     */
    @RequestMapping(value = "/confirmRegistration", method = RequestMethod.GET)
    public ResponseEntity<?> confirmRegistration(@RequestParam("token") String token,
                                                 @RequestParam("orderId") Optional<Long> orderId)
            throws TokenNotFoundException, InvalidTokenException {

        VerificationToken verificationToken =
                verificationTokenRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));

        // Get the user associated with this token
        User user = verificationToken.getUser();
        if (!verificationToken.isValid()) {
            throw new InvalidTokenException();
        }

        userService.verify(user.getId());
        verificationToken.use();

        //Disable the token for future use.
        verificationTokenRepository.saveAndFlush(verificationToken);

        //Bind the order to the user
        orderId.ifPresent(id -> orderService.assignOrderToUser(id, user.getUsername()));

        return createResponseEntity(HttpStatus.OK, "Succesfully verified");
    }
}
