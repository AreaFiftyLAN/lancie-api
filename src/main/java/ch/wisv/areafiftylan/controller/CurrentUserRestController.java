package ch.wisv.areafiftylan.controller;


import ch.wisv.areafiftylan.dto.UserDTO;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("/users/current")
@PreAuthorize("isAuthenticated()")
public class CurrentUserRestController {

    private UserService userService;

    @Autowired
    CurrentUserRestController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get the User currently logged in. Because our User model implements the Spring Security UserDetails, this can be
     * directly derived from the Authentication object which is automatically added. Returns a not-found entity if
     * there's no user logged in. Returns the user
     *
     * @param auth Current Authentication object, automatically taken from the SecurityContext
     *
     * @return The currently logged in User.
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        // Get the currently logged in user from the autowired Authentication object.
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user = userService.getUserByUsername(currentUser.getUsername()).get();
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * This method accepts PUT requests on /users/current. It replaces all fields with the new user provided in the
     * RequestBody and resets the profile fields. All references to the old user are maintained (Team membership ect).
     *
     * @param input A UserDTO object containing data of the new user
     *
     * @return The User object.
     */
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<?> replaceCurrentUser(@Validated @RequestBody UserDTO input, Authentication auth) {
        User user = (User) auth.getPrincipal();
        user = userService.replace(user.getId(), input);
        return createResponseEntity(HttpStatus.OK, "User successfully replaced", user);
    }

    @RequestMapping(method = RequestMethod.PATCH)
    public ResponseEntity<?> updateCurrentUser(@Validated @RequestBody UserDTO input, Authentication auth) {
        //TODO: Differentiate between PATCH and PUT
        User user = (User) auth.getPrincipal();
        user = userService.replace(user.getId(), input);
        return createResponseEntity(HttpStatus.OK, "User successfully replaced", user);
    }
}
