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


import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.model.order.Order;
import ch.wisv.areafiftylan.products.service.OrderService;
import ch.wisv.areafiftylan.products.service.TicketService;
import ch.wisv.areafiftylan.seats.model.Seat;
import ch.wisv.areafiftylan.seats.service.SeatService;
import ch.wisv.areafiftylan.security.authentication.PasswordChangeDTO;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.teams.model.TeamInviteResponse;
import ch.wisv.areafiftylan.teams.service.TeamService;
import ch.wisv.areafiftylan.users.model.ProfileDTO;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.service.UserService;
import ch.wisv.areafiftylan.utils.view.View;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("/users/current")
@PreAuthorize("isAuthenticated()")
public class CurrentUserRestController {

    private final UserService userService;
    private final SeatService seatService;
    private final TeamService teamService;
    private final OrderService orderService;
    private final TicketService ticketService;

    @Autowired
    CurrentUserRestController(UserService userService, OrderService orderService, TicketService ticketService,
                              TeamService teamService, SeatService seatService) {
        this.userService = userService;
        this.seatService = seatService;
        this.orderService = orderService;
        this.ticketService = ticketService;
        this.teamService = teamService;
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
    @GetMapping
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        // To prevent 403 errors on this endpoint, we manually handle unauthenticated users, instead of a
        // preauthorize tag.
        if (auth != null) {
            // Get the currently logged in user from the autowired Authentication object.
            UserDetails currentUser = (UserDetails) auth.getPrincipal();
            User user = userService.getUserByUsername(currentUser.getUsername());
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return createResponseEntity(HttpStatus.OK, "Not logged in");
        }
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
    @RequestMapping(value = "/profile", method = RequestMethod.POST)
    public ResponseEntity<?> addProfile(@Validated @RequestBody ProfileDTO input, Authentication auth) {
        User user = userService.addProfile(((User) auth.getPrincipal()).getId(), input);
        return createResponseEntity(HttpStatus.OK, "Profile successfully set", user.getProfile());
    }

    /**
     * This mapping allows the user to change their password while logged in. This is different from the password reset
     * functionality which works with tokens. Users have to provide both their old and new password, and have to be
     * fully authenticated, meaning that they can't be coming from a "Remember me" session.
     *
     * @param auth              The current user
     * @param passwordChangeDTO DTO containing oldPassword and newPassword
     *
     * @return Statusmessage
     */
    @RequestMapping(value = "/password", method = RequestMethod.POST)
    public ResponseEntity<?> changeCurrentUserPassword(Authentication auth,
                                                       @RequestBody @Validated PasswordChangeDTO passwordChangeDTO) {
        User currentUser = (User) auth.getPrincipal();

        userService.changePassword(currentUser.getId(), passwordChangeDTO.getOldPassword(),
                passwordChangeDTO.getNewPassword());

        return createResponseEntity(HttpStatus.OK, "Password successfully changed");
    }

    /**
     * Get all the Teams the current user is a member of.
     *
     * @param auth Current Authentication object, automatically taken from the SecurityContext
     *
     * @return A Collection of Teams of which the current User is a member
     */
    @JsonView(View.Team.class)
    @RequestMapping(value = "/teams", method = RequestMethod.GET)
    public Collection<Team> getCurrentTeams(Authentication auth) {
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        return teamService.getTeamsByUsername(currentUser.getUsername());
    }

    @RequestMapping(value = "/teams/invites", method = RequestMethod.GET)
    public List<TeamInviteResponse> getOpenInvites(Authentication auth) {
        User currentUser = (User) auth.getPrincipal();

        return teamService.findTeamInvitesByUsername(currentUser.getUsername());
    }

    /**
     * Get all Orders that the current User created. This doesn't include expired orders. This will be a Collection with
     * size 0 or 1 of the majority, but it can contain more orders.
     *
     * @param auth The current User
     *
     * @return A collection of Orders of the current User.
     */
    @JsonView(View.OrderOverview.class)
    @RequestMapping(value = "/orders", method = RequestMethod.GET)
    public Collection<Order> getAllOrders(Authentication auth) {
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        return orderService.findOrdersByUsername(currentUser.getUsername());
    }

    /**
     * Get the tickets of the currently logged in user. All the tickets owned by this user will be returned.
     *
     * @param auth The current User, injected by spring
     *
     * @return The current owned tickets, if any exist
     */
    @RequestMapping(value = "/tickets", method = RequestMethod.GET)
    public Collection<Ticket> getAllTickets(Authentication auth) {
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        return ticketService.findValidTicketsByOwnerUsername(currentUser.getUsername());
    }

    /**
     * Get the current open order include expired orders. A User van only have one open order.
     *
     * @param auth The current User, injected by Spring
     *
     * @return The current open order, if any exist
     */
    @RequestMapping(value = "/orders/open", method = RequestMethod.GET)
    public List<Order> getOpenOrder(Authentication auth) {
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        return orderService.getOpenOrders(currentUser.getUsername());
    }

    /**
     * Get the seats of the current user
     *
     * @param auth Currently logged in user
     *
     * @return Returns a list of reserved seats by the user
     */
    @RequestMapping(value = "/seat", method = RequestMethod.GET)
    public List<Seat> getCurrentUserSeat(Authentication auth) {
        User user = (User) auth.getPrincipal();

        return seatService.getSeatsByUsername(user.getUsername());
    }
}
