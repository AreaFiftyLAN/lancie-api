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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("/users/current")
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
     * automatically resolved from the current Authentication.getPrincipal(). Returns a not-found entity if
     * there's no user logged in. Returns the user.
     *
     * @param user automatically resolve the current Authentication.getPrincipal() for Spring MVC arguments.
     *
     * @return The currently logged in User.
     */
    @GetMapping
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User user) {
        // To prevent 403 errors on this endpoint, we manually handle unauthenticated users, instead of a
        // preauthorize tag.
        if (user == null) {
            return createResponseEntity(HttpStatus.OK, "Not logged in.");
        } else {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
    }

    /**
     * Add a profile to the current user. An empty profile is created when a user is created, so this method fills the
     * existing fields.
     *
     * @param input A representation of the profile.
     *
     * @return The user with the new profile.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/profile")
    public ResponseEntity<?> addProfile(@AuthenticationPrincipal User user, @Validated @RequestBody ProfileDTO input) {
        user = userService.addProfile(user.getId(), input);
        return createResponseEntity(HttpStatus.OK, "Profile successfully set", user.getProfile());
    }

    /**
     * This mapping allows the user to change their password while logged in. This is different from the password reset
     * functionality which works with tokens. Users have to provide both their old and new password, and have to be
     * fully authenticated, meaning that they can't be coming from a "Remember me" session.
     *
     * @param passwordChangeDTO DTO containing oldPassword and newPassword.
     *
     * @return Statusmessage of the request.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/password")
    public ResponseEntity<?> changeCurrentUserPassword(@AuthenticationPrincipal User user, @RequestBody @Validated PasswordChangeDTO passwordChangeDTO) {
        userService.changePassword(user.getId(), passwordChangeDTO.getOldPassword(), passwordChangeDTO.getNewPassword());
        return createResponseEntity(HttpStatus.OK, "Password successfully changed");
    }

    /**
     * Get all the Teams the current user is a member of.
     *
     * @return A Collection of Teams of which the current User is a member.
     */
    @JsonView(View.Team.class)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/teams")
    public Collection<Team> getCurrentTeams(@AuthenticationPrincipal User user) {
        return teamService.getTeamsByUsername(user.getUsername());
    }

    /**
     * Get all invites of all Teams the current user is a member of.
     *
     * @return A List of TeamInviteResponse's of your teams.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/teams/invites")
    public List<TeamInviteResponse> getOpenInvites(@AuthenticationPrincipal User user) {
        return teamService.findTeamInvitesByUsername(user.getUsername());
    }

    /**
     * Get all Orders that the current User created. This doesn't include expired orders. This will be a Collection with
     * size 0 or 1 of the majority, but it can contain more orders.
     *
     * @return A collection of Orders of the current User.
     */
    @JsonView(View.OrderOverview.class)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/orders")
    public Collection<Order> getAllOrders(@AuthenticationPrincipal User user) {
        return orderService.findOrdersByUsername(user.getUsername());
    }

    /**
     * Get the tickets of the currently logged in user. All the tickets owned by this user will be returned.
     *
     * @return The current owned tickets, if any exist.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/tickets")
    public Collection<Ticket> getAllTickets(@AuthenticationPrincipal User user) {
        return ticketService.findValidTicketsByOwnerUsername(user.getUsername());
    }

    /**
     * Get the current open order include expired orders. A User van only have one open order.
     *
     * @return The current open order, if any exist.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/orders/open")
    public List<Order> getOpenOrder(@AuthenticationPrincipal User user) {
        return orderService.getOpenOrders(user.getUsername());
    }

    /**
     * Get the seats of the current user.
     *
     * @return Returns a list of reserved seats by the user.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/seat")
    public List<Seat> getCurrentUserSeat(@AuthenticationPrincipal User user) {
        return seatService.getSeatsByUsername(user.getUsername());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<?> adminCheck() {
        return createResponseEntity(HttpStatus.OK, "You are an admin.");
    }
}
