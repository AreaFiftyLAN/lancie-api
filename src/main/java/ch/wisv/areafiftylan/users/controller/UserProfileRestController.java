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

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("/users")
public class UserProfileRestController {

    private final UserService userService;

    @Autowired
    UserProfileRestController(UserService userService) {
        this.userService = userService;
    }

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
    @PostMapping("/{userId}/profile")
    public ResponseEntity<?> addProfile(@PathVariable Long userId, @Validated @RequestBody ProfileDTO input) {
        User user = userService.addProfile(userId, input);

        return createResponseEntity(HttpStatus.OK, "Profile successfully set", user.getProfile());
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
    @PostMapping("/current/profile")
    public ResponseEntity<?> addProfile(@AuthenticationPrincipal User user, @Validated @RequestBody ProfileDTO input) {
        return this.addProfile(user.getId(), input);
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
    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> changeProfile(@PathVariable Long userId, @Validated @RequestBody ProfileDTO input) {
        User user = userService.changeProfile(userId, input);

        return createResponseEntity(HttpStatus.OK, "Profile successfully updated", user.getProfile());
    }

    /**
     * Resets the profile fields to null. The profile can't actually be deleted as it is a required field.
     *
     * @param userId The userId of the user which needs the profile reset
     *
     * @return Empty body with StatusCode OK.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}/profile")
    public ResponseEntity<?> resetProfile(@PathVariable Long userId) {
        Profile profile = userService.resetProfile(userId);
        return createResponseEntity(HttpStatus.OK, "Profile successfully reset", profile);
    }
}
