/*
 * Copyright (c) 2018  W.I.S.V. 'Christiaan Huygens'
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

import ch.wisv.areafiftylan.exception.TokenNotFoundException;
import ch.wisv.areafiftylan.products.service.OrderService;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.service.UserService;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;


/**
 * This class handles all authentication related requests. These requests deal with the login message, the user email
 * verification and the password resets. Due to the minimal logic involved with tokens, it interacts directly with their
 * respective repositories.
 */
@Controller
@Slf4j
public class AuthenticationController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final OrderService orderService;


    @Autowired
    public AuthenticationController(UserService userService, AuthenticationService authenticationService,
                                    OrderService orderService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
        this.orderService = orderService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/token/verify")
    public ResponseEntity<?> verifyToken() {
        return createResponseEntity(HttpStatus.OK, "Token is valid!");
    }

    /**
     * This method requests a passwordResetToken and sends it to the user. With this token, the user can reset his
     * password.
     *
     * @param body The body, should only contain an email parameter TODO: This can be done more elegantly
     *
     * @return A status message telling whether the action was successful
     */
    @PostMapping("/requestResetPassword")
    @ResponseBody
    public ResponseEntity<?> requestResetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        try {
            User user = userService.getUserByEmail(email);
            userService.requestResetPassword(user);
            log.info("Requested password reset for User {}", user.getId(),
                    StructuredArguments.v("user_id", user.getId()));
        } catch (UsernameNotFoundException e) {
            log.warn("Password for {} can't be reset, User doesn't exist", email,
                    StructuredArguments.v("user_email", email));
        }


        return createResponseEntity(HttpStatus.OK,
                "If you're registered, a password reset link has been sent to " + email);
    }

    /**
     * After requesting a token, the user can reset his password with a POST call to this method. It should contain the
     * token and a password. The user is derived from the token.
     *
     * @param body The body, containing a token and password parameter.
     *
     * @return A status message telling whether the action was successful
     */
    @PostMapping("/resetPassword")
    @ResponseBody
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String password = body.get("password");

        authenticationService.resetPasswordByToken(token, password);

        return createResponseEntity(HttpStatus.OK, "Password set!");
    }

    /**
     * This function confirms the registration of a user based on the provided token. If retrieves the token from the
     * repository. From this token, we get the user. For this user, the enabled flag is set to true. After this, the
     * token is marked as used so it can not be used again.
     * <p>
     *
     * @param token The token as generated for the user opon registration
     *
     * @return A status message containing information about the operation
     *
     * @throws TokenNotFoundException if the token can't be found
     */
    @GetMapping("/confirmRegistration")
    public ResponseEntity<?> confirmRegistration(@RequestParam("token") String token,
                                                 @RequestParam(value = "orderId", required = false) Long orderId) {

        User user = authenticationService.verifyUserByToken(token);

        //Bind the order to the user
        if (orderId != null) {
            orderService.assignOrderToUser(orderId, user.getEmail());
        }

        return createResponseEntity(HttpStatus.OK, "Successfully verified");
    }
}
