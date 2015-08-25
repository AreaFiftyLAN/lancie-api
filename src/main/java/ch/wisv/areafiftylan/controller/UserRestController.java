package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.ResponseEntityBuilder;
import ch.wisv.areafiftylan.dto.UserDTO;
import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.SeatService;
import ch.wisv.areafiftylan.service.UserService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    User getUserById(@PathVariable Long userId) {
        return this.userService.getUserById(userId).get();
    }

    /**
     * Get all users in the database
     *
     * @return all users
     */
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

    //////////// OTHER MAPPINGS //////////////////

    @RequestMapping(value = "/{userId}/seat", method = RequestMethod.GET)
    Seat getSeatByUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId).get();

        return seatService.getSeatByUser(user);
    }

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
