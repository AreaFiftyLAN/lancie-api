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

package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.ProfileDTO;
import ch.wisv.areafiftylan.model.Profile;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("/users")
public class UserProfileRestController {

    private UserService userService;

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
    @RequestMapping(value = "/{userId}/profile", method = RequestMethod.POST)
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
}
