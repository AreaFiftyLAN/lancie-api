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

package ch.wisv.areafiftylan.users.controller;

import ch.wisv.areafiftylan.seats.model.Seat;
import ch.wisv.areafiftylan.seats.service.SeatService;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.model.UserDTO;
import ch.wisv.areafiftylan.users.service.UserService;
import ch.wisv.areafiftylan.utils.ResponseEntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.Collection;
import java.util.List;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("/users")
public class UserRestController {

    private final UserService userService;

    private final SeatService seatService;

    @Autowired
    UserRestController(UserService userService, SeatService seatService) {
        this.userService = userService;
        this.seatService = seatService;
    }

    /**
     * This method accepts POST requests on /users. It will send the input to the {@link UserService} to create a new
     * user
     *
     * @param input The user that has to be created. It consists of 2 fields. The the email and the plain-text
     *              password. The password is saved hashed using the BCryptPasswordEncoder
     *
     * @return The generated object, in JSON format.
     */
    @PostMapping
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
    @PutMapping("/{userId}")
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
    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId) {
        return this.userService.getUserById(userId);
    }

    /**
     * Get all users in the database. Requires ADMIN privileges.
     *
     * @return all users
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Collection<User> readUsers() {
        return userService.getAllUsers();
    }

    /**
     * Get the User currently logged in. Because our User model implements the Spring Security UserDetails, this can be
     * directly derived from the Authentication object which is automatically added. Returns a not-found entity if
     * there's no user logged in. Returns the user
     *
     * @param user the current user
     * @return The currently logged in User.
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User user) {
        // To prevent 403 errors on this endpoint, we manually handle unauthenticated users, instead of @PreAuthorize.
        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return createResponseEntity(HttpStatus.OK, "Not logged in");
        }
    }

    /**
     * Users can't actually be deleted due to various (security) constraints. It will be marked as disabled instead.
     *
     * @param userId User to be disabled
     *
     * @return A status message.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> disableUser(@PathVariable Long userId) {
        userService.lock(userId);
        return createResponseEntity(HttpStatus.OK, "User disabled");
    }

    @PreAuthorize("hasRole('ADMIN') OR hasRole('COMMITTEE')")
    @GetMapping("/{userId}/alcoholcheck")
    public ResponseEntity<?> alcoholCheck(@PathVariable Long userId) {
        boolean oldEnough = userService.alcoholCheck(userId);
        return ResponseEntityBuilder.createResponseEntity(HttpStatus.OK, "Age checked!", oldEnough);
    }

    /**
     * Get the Seat of a specific User
     *
     * @param userId Id of the User you want the Seat of.
     *
     * @return The Seat of the given User.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}/seat")
    public List<Seat> getSeatByUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return seatService.getSeatsByEmail(user.getEmail());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return createResponseEntity(HttpStatus.CONFLICT, "Email is already in use");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
        return createResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
