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

package ch.wisv.areafiftylan.users.controller;

import ch.wisv.areafiftylan.extras.rfid.service.RFIDService;
import ch.wisv.areafiftylan.users.model.Profile;
import ch.wisv.areafiftylan.users.model.ProfileDTO;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("/users")
public class UserProfileRestController {

    private final UserService userService;
    private final RFIDService rfidService;

    @Autowired
    UserProfileRestController(UserService userService, RFIDService rfidService) {
        this.userService = userService;
        this.rfidService = rfidService;
    }

    /**
     * This method returns a boolean indicating whether an user is allowed to make the changes to
     * their profile or not.
     *
     * @param user The concerning user
     * @param input The changes
     * @return boolean whether they are allowed to edit their profile
     */
    private boolean allowedToEditProfile(User user, ProfileDTO input) {
        boolean isUserCheckedIn = rfidService.isOwnerLinked(user.getEmail());
        LocalDate currentBirthday = user.getProfile().getBirthday();
        boolean isDateChanged = !currentBirthday.equals(input.getBirthday());


        return !isDateChanged || !isUserCheckedIn;
    }

    /**
     * Add a profile to the current user. An empty profile is created when a user is created, so
     * this method fills the existing fields.
     * <p>
     * This method is also called when users change their profile. It is unwanted behaviour that
     * users can change their birth date during the event. This is checked before writing the
     * changes in the function
     *
     * @param input A representation of the profile
     * @return The user with the new profile
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/current/profile")
    public ResponseEntity<?> addProfile(@AuthenticationPrincipal User user, @Validated @RequestBody ProfileDTO input) {
        if (allowedToEditProfile(user, input)) {
            User changedUser = userService.addProfile(user.getId(), input);
            return createResponseEntity(HttpStatus.OK, "Profile successfully changed", changedUser.getProfile());
        } else {
            return createResponseEntity(HttpStatus.BAD_REQUEST, "Not permitted to change date during event", user.getProfile());
        }
    }

    /**
     * Add a profile to a user. An empty profile is created when a user is created, so this method
     * fills the existing fields
     *
     * @param userId The userId of the user to which the profile needs to be added
     * @param input  A representation of the profile
     * @return The user with the new profile
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessUser(principal, #userId)")
    @PostMapping("/{userId}/profile")
    public ResponseEntity<?> addProfile(@PathVariable Long userId, @Validated @RequestBody ProfileDTO input) {
        User user = userService.getUserById(userId);

        return this.addProfile(user, input);
    }

    /**
     * Change the profile fields of the User. Basically the same as the POST request.
     *
     * @param userId The userId of the user to which the profile needs to be added
     * @param input  A representation of the profile
     * @return The user with the changed profile
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessUser(principal, #userId)")
    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> changeProfile(@PathVariable Long userId, @Validated @RequestBody ProfileDTO input) {
        User user = userService.getUserById(userId);

        return this.addProfile(user, input);
    }

    /**
     * Resets the profile fields to null. The profile can't actually be deleted as it is a required
     * field.
     *
     * @param userId The userId of the user which needs the profile reset
     * @return Empty body with StatusCode OK.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}/profile")
    public ResponseEntity<?> resetProfile(@PathVariable Long userId) {
        Profile profile = userService.resetProfile(userId);
        return createResponseEntity(HttpStatus.OK, "Profile successfully reset", profile);
    }
}
