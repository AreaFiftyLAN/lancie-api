package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.ProfileDTO;
import ch.wisv.areafiftylan.dto.UserDTO;
import ch.wisv.areafiftylan.exception.UserNotFoundException;
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
     * This method accepts PUT requests on /users/current. It replaces all fields with the new user provided in the
     * RequestBody and resets the profile fields. All references to the old user are maintained (Team membership ect).
     *
     * @param input A UserDTO object containing data of the new user
     *
     * @return The User object.
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/current", method = RequestMethod.PUT)
    public ResponseEntity<?> replaceCurrentUser(@Validated @RequestBody UserDTO input, Authentication auth) {
        User user = (User) auth.getPrincipal();
        user = userService.replace(user.getId(), input);
        return createResponseEntity(HttpStatus.OK, "User successfully replaced", user);
    }

    /**
     * Edit the current user. Only change the fields which have been set. All fields should be in the requestbody.
     * <p>
     * TODO: The UserDTO needs to be valid right now, this should allow for null fields.
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
     * Get the User currently logged in. Because our User model implements the Spring Security UserDetails, this can be
     * directly derived from the Authentication object which is automatically added. Returns a not-found entity if
     * there's no user logged in. Returns the user
     *
     * @param auth Current Authentication object, automatically taken from the SecurityContext
     *
     * @return The currently logged in User.
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/current", method = RequestMethod.GET)
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        // Get the currently logged in user from the autowired Authentication object.
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user = userService.getUserByUsername(currentUser.getUsername()).get();
        return new ResponseEntity<>(user, HttpStatus.OK);
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

        // Create HttpHeaders to include the location of the newly created profile
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}/profile").buildAndExpand(user.getId())
                        .toUri());

        return createResponseEntity(HttpStatus.CREATED, httpHeaders,
                "Profile succesfully created at" + httpHeaders.getLocation(), user.getProfile());
    }

    /**
     * Add a profile to the current user. An empty profile is created when a user is created, so this method fills the
     * existing fields
     *
     * @param input A representation of the profile
     *
     * @return The user with the new profile
     */
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
     * Get the profile from the currently logged on user
     *
     * @param auth Authentication of the current user
     *
     * @return The profile of the currently logged on user
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/current/profile", method = RequestMethod.GET)
    public ResponseEntity<?> readCurrentProfile(Authentication auth) {
        if (auth != null) {
            User user = (User) auth.getPrincipal();
            Profile profile = userService.getUserById(user.getId()).
                    orElseThrow(() -> new UserNotFoundException(user.getId())).getProfile();
            return new ResponseEntity<>(profile, HttpStatus.OK);
        } else {
            return createResponseEntity(HttpStatus.NOT_FOUND, null, "No user currently logged in!", null);
        }
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
        return createResponseEntity(HttpStatus.FORBIDDEN, "Access denied");
    }
}
