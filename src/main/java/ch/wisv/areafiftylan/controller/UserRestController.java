package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.ProfileDTO;
import ch.wisv.areafiftylan.dto.UserDTO;
import ch.wisv.areafiftylan.model.Profile;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.UserService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    public ResponseEntity<?> add(@Validated @RequestBody UserDTO input) {
        User save = userService.create(input);

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

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/current", method = RequestMethod.PUT)
    public ResponseEntity<?> replaceCurrentUser(@Validated @RequestBody UserDTO input, Authentication auth) {
        User user = (User) auth.getPrincipal();
        user = userService.replace(user.getId(), input);
        return createResponseEntity(HttpStatus.OK, "User successfully replaced", user);
    }

    /**
     * Edit the current user. Only change the fields which have been set. All fields should be in the requestbody.
     *
     * @param userId The id of the user to be updated
     * @param input  A userDTO object with updated fields. empty fields will be ignored
     *
     * @return The updated User object
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessUser(principal, #userId)")
    @RequestMapping(value = "/{userId}", method = RequestMethod.PATCH)
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UserDTO input) {
        //TODO: Differentiate between PATCH and PUT
        User user = this.userService.replace(userId, input);
        return createResponseEntity(HttpStatus.OK, "User successfully updated", user);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/current", method = RequestMethod.PATCH)
    public ResponseEntity<?> updateCurrentUser(@Validated @RequestBody UserDTO input, Authentication auth) {
        //TODO: Differentiate between PATCH and PUT
        User user = (User) auth.getPrincipal();
        user = userService.replace(user.getId(), input);
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
        return this.userService.getUserById(userId).get();
    }

    /**
     * Get the User currently logged in. Returns a not-found entity if there's no user logged in. Returns the user
     *
     * @param auth Current Authentication object, automatically taken from the SecurityContext
     *
     * @return The currently logged in User.
     */
    @RequestMapping(value = "/current", method = RequestMethod.GET)
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        if (auth != null) {
            UserDetails currentUser = (UserDetails) auth.getPrincipal();
            User user = userService.getUserByUsername(currentUser.getUsername()).get();
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return createResponseEntity(HttpStatus.NOT_FOUND, null, "No user currently logged in!", null);
        }

    }

    /**
     * Get all users in the database
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
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userService.lock(userId, true);
        return createResponseEntity(HttpStatus.OK, "User disabled");
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
    @PreAuthorize("@currentUserServiceImpl.canAccessUser(principal, #userId)")
    @RequestMapping(value = "/{userId}/profile", method = RequestMethod.POST)
    public ResponseEntity<?> addProfile(@PathVariable Long userId, @Validated @RequestBody ProfileDTO input) {
        User user = userService.addProfile(userId, input);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}/profile").buildAndExpand(user.getId())
                        .toUri());

        return createResponseEntity(HttpStatus.CREATED, httpHeaders,
                "Profile succesfully created at" + httpHeaders.getLocation(), user.getProfile());
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/current/profile", method = RequestMethod.POST)
    public ResponseEntity<?> addProfile(@Validated @RequestBody ProfileDTO input, Authentication auth) {
        return this.addProfile(((User) auth.getPrincipal()).getId(), input);
    }

    /**
     * Change the profile fields of the User. Basically the same as the POST request.
     *
     * @param userId The userId of the user to which the profile needs to be added
     * @param input  A representation of the profile
     *
     * @return The user with the changed profile
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessUser(principal, #userId)")
    @RequestMapping(value = "/{userId}/profile", method = RequestMethod.PUT)
    public ResponseEntity<?> changeProfile(@PathVariable Long userId, @Validated @RequestBody ProfileDTO input) {
        User user = userService.changeProfile(userId, input);

        return createResponseEntity(HttpStatus.OK, "Profile successfully updated", user.getProfile());
    }

    /**
     * Get the profile view without the related User
     *
     * @param userId UserId of the user
     *
     * @return The profile of the specific user
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessUser(principal, #userId)")
    @RequestMapping(value = "/{userId}/profile", method = RequestMethod.GET)
    public Profile readProfile(@PathVariable Long userId) {
        return userService.getUserById(userId).get().getProfile();
    }

    /**
     * Resets the profile fields to null. The profile can't actually be deleted as it is a required field.
     *
     * @param userId The userId of the user which needs the profile reset
     *
     * @return Empty body with StatusCode OK.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/{userId}/profile", method = RequestMethod.DELETE)
    public ResponseEntity<?> resetProfile(@PathVariable Long userId) {
        Profile profile = userService.resetProfile(userId);
        return createResponseEntity(HttpStatus.OK, "Profile successfully reset", profile);
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

        return createResponseEntity(HttpStatus.CONFLICT, null, message, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        return createResponseEntity(HttpStatus.UNAUTHORIZED, "Access denied, please log in");
    }
}
