package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.UserDTO;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.UserService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("/users")
public class UserRestController {

    private UserService userService;

    @Autowired
    UserRestController(UserService userService) {
        this.userService = userService;
    }

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
    public ResponseEntity<?> add(HttpServletRequest request, @Validated @RequestBody UserDTO input) {
        User save = userService.create(input, request);

        // Create headers to set the location of the created User object.
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(save.getId()).toUri());

        return createResponseEntity(HttpStatus.CREATED, httpHeaders,
                "User successfully created at " + httpHeaders.getLocation(), save);
    }

    /**
     * This method accepts PUT requests on /users/{userId}. It replaces all fields with the new user provided in the
     * RequestBody and resets the profile fields. All references to the old user are maintained (Team membership ect).
     *
     * @param userId The userId of the User to be replaced
     * @param input  A UserDTO object containing data of the new user
     *
     * @return The User object.
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessUser(principal, #userId)")
    @RequestMapping(value = "/{userId}", method = RequestMethod.PUT)
    public ResponseEntity<?> replaceUser(@PathVariable Long userId, @Validated @RequestBody UserDTO input) {
        User user = userService.replace(userId, input);
        return createResponseEntity(HttpStatus.OK, "User successfully replaced", user);
    }

    /**
     * Get the user with a specific userId
     *
     * @param userId The user to be retrieved
     *
     * @return The user with the given userId
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessUser(principal, #userId)")
    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    public User getUserById(@PathVariable Long userId) {
        return this.userService.getUserById(userId);
    }

    /**
     * Get all users in the database. Requires ADMIN privileges.
     *
     * @return all users
     */
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(method = RequestMethod.GET)
    public Collection<User> readUsers() {
        return userService.getAllUsers();
    }

    /**
     * Users can't actually be deleted due to various (security) constraints. It will be marked as disabled instead.
     *
     * @param userId User to be disabled
     *
     * @return A status message.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> disableUser(@PathVariable Long userId) {
        userService.lock(userId);
        return createResponseEntity(HttpStatus.OK, "User disabled");
    }

    /**
     * Checks for the availability of an email address. Returns false when another user is already registered with this
     * email.
     *
     * @param email The emailaddress to be checked.
     *
     * @return Whether this emailaddress has already been registered.
     */
    @RequestMapping(value = "/checkEmail", method = RequestMethod.GET)
    public Boolean checkEmailExists(@RequestParam String email) {
        return userService.checkEmailAvailable(email);
    }

    /**
     * Checks for the availability of a username. Returns false when another user is already registered with this
     * username.
     *
     * @param username The username to be checked.
     *
     * @return Whether this username has already been registered.
     */
    @RequestMapping(value = "/checkUsername", method = RequestMethod.GET)
    public Boolean checkUsernameExists(@RequestParam String username) {
        return userService.checkUsernameAvailable(username);
    }

    //////////// EXCEPTION HANDLING //////////////////

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        HttpStatus status;
        String message = "Database constraint violated!";

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

        return createResponseEntity(HttpStatus.CONFLICT, message);
    }
}
