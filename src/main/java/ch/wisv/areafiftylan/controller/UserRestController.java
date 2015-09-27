package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.ProfileDTO;
import ch.wisv.areafiftylan.dto.UserDTO;
import ch.wisv.areafiftylan.model.Profile;
import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.security.CurrentUser;
import ch.wisv.areafiftylan.service.SeatService;
import ch.wisv.areafiftylan.service.UserService;
import ch.wisv.areafiftylan.util.ResponseEntityBuilder;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;

@RestController
@RequestMapping("/users")
public class UserRestController {


    private UserService userService;

    private SeatService seatService;

    @Autowired
    UserRestController(UserService userService, SeatService seatService) {
        this.userService = userService;
        this.seatService = seatService;
    }

    //////////// USER MAPPINGS //////////////////

    /**
     * This method accepts POST requests on /users. It will send the input to the {@link UserService} to create a new
     * user
     *
     * @param input The user that has to be created. It consists of 3 fields. The username, the email and the plain-text
     *              password. The password is saved hashed using the BCryptPasswordEncoder
     *
     * @return The generated object, in JSON format.
     */
    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(@Validated @RequestBody UserDTO input) {
        User save = userService.create(input);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(save.getId()).toUri());

        return ResponseEntityBuilder.createResponseEntity(HttpStatus.CREATED, httpHeaders,
                "User successfully created at " + httpHeaders.getLocation(), save);
    }

    /**
     * This method accepts PUT requests on /users/{userId}. It replaces all fields with the new user provided in the
     * RequestBody and resets the profile fields. All references to the old user are maintained (Team membership ect).
     *
     * @param userId The userId of the User to be repalced
     * @param input  A UserDTO object containing data of the new user
     *
     * @return The User object.
     */
    @RequestMapping(value = "/{userId}", method = RequestMethod.PUT)
    ResponseEntity<?> replaceUser(@PathVariable Long userId, @Validated @RequestBody UserDTO input) {
        User user = this.userService.replace(userId, input);
        return ResponseEntityBuilder.createResponseEntity(HttpStatus.OK, null, "User successfully replaced", user);
    }

    /**
     * Edit the current user. Only change the fields which have been set. All fields should be in the requestbody.
     *
     * @param userId The id of the user to be updated
     * @param input  A userDTO object with updated fields. empty fields will be ignored
     *
     * @return The updated User object
     */
    @RequestMapping(value = "/{userId}", method = RequestMethod.PATCH)
    ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UserDTO input) {
        User user = this.userService.replace(userId, input);
        return ResponseEntityBuilder.createResponseEntity(HttpStatus.OK, null, "User successfully updated", user);
    }

    /**
     * Get the user with a specific userId
     *
     * @param userId The user to be retrieved
     *
     * @return The user with the given userId
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessUser(principal, #id)")
    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    User getUserById(@PathVariable Long userId) {
        return this.userService.getUserById(userId).get();
    }

    @RequestMapping(value = "/current", method = RequestMethod.GET)
    User getCurrentUser(Authentication auth) {
        CurrentUser currentUser = (CurrentUser) auth.getPrincipal();
        return currentUser.getUser();
    }

    /**
     * Get all users in the database
     *
     * @return all users
     */
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(method = RequestMethod.GET)
    Collection<User> readUsers() {
        return userService.getAllUsers();
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        User deletedUser = userService.getUserById(userId).get();
        userService.delete(userId);
        return ResponseEntityBuilder
                .createResponseEntity(HttpStatus.OK, null, "User successfully deleted", deletedUser);
    }

    //////////// PROFILE MAPPINGS //////////////////


    /**
     * Add a profile to a user. An empty profile is created when a user is created, so this method fills the existing
     * fields
     *
     * @param userId The userId of the user to which the profile needs to be added
     * @param input  A representation of the profile
     *
     * @return The user with the new profile
     */
    @RequestMapping(value = "/{userId}/profile", method = RequestMethod.POST)
    ResponseEntity<?> addProfile(@PathVariable Long userId, @Validated @RequestBody ProfileDTO input) {
        User user = userService.addProfile(userId, input);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}/profile").buildAndExpand(user.getId())
                        .toUri());

        return new ResponseEntity<>(user, httpHeaders, HttpStatus.CREATED);
    }

    /**
     * Change the profile fields of the User. Basically the same as the POST request.
     *
     * @param userId The userId of the user to which the profile needs to be added
     * @param input  A representation of the profile
     *
     * @return The user with the changed profile
     */
    @RequestMapping(value = "/{userId}/profile", method = RequestMethod.PUT)
    ResponseEntity<?> changeProfile(@PathVariable Long userId, @Validated @RequestBody ProfileDTO input) {
        User user = userService.changeProfile(userId, input);

        return new ResponseEntity<>(user, new HttpHeaders(), HttpStatus.OK);
    }

    /**
     * Get the profile view without the related User
     *
     * @param userId UserId of the user
     *
     * @return The profile of the specific user
     */
    @RequestMapping(value = "/{userId}/profile", method = RequestMethod.GET)
    Profile readProfile(@PathVariable Long userId) {
        return userService.getUserById(userId).get().getProfile();
    }

    /**
     * Resets the profile fields to null. The profile can't actually be deleted as it is a required field.
     *
     * @param userId The userId of the user which needs the profile reset
     *
     * @return Empty body with StatusCode OK.
     */
    @RequestMapping(value = "/{userId}/profile", method = RequestMethod.DELETE)
    ResponseEntity<?> resetProfile(@PathVariable Long userId) {
        Profile profile = userService.resetProfile(userId);
        return new ResponseEntity<>(profile, new HttpHeaders(), HttpStatus.OK);
    }

    //////////// OTHER MAPPINGS //////////////////

    @RequestMapping(value = "/{userId}/seat", method = RequestMethod.GET)
    Seat getSeatByUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId).get();

        return seatService.getSeatByUser(user);
    }

    //////////// EXCEPTION HANDLING //////////////////

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        HttpStatus status;
        String message = "Database contraint violated!";

        try {
            throw ex.getCause();
        } catch (ConstraintViolationException constraintException) {
            String constraintName = constraintException.getConstraintName();
            if ("USERNAME".equals(constraintName)) {
                message = "Username is not unique!";
            } else if ("EMAIL".equals(constraintName)) {
                message = "Email is not unique!";
            }
        } catch (Throwable throwable) {
            message = throwable.toString();
        }

        return ResponseEntityBuilder.createResponseEntity(HttpStatus.CONFLICT, null, message, null);
    }
}
