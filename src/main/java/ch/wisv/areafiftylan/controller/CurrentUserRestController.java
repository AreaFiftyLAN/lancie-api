package ch.wisv.areafiftylan.controller;


import ch.wisv.areafiftylan.dto.PasswordChangeDTO;
import ch.wisv.areafiftylan.dto.TeamInviteResponse;
import ch.wisv.areafiftylan.model.*;
import ch.wisv.areafiftylan.model.view.View;
import ch.wisv.areafiftylan.service.*;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("/users/current")
public class CurrentUserRestController {

    private UserService userService;
    private SeatService seatService;
    private TeamService teamService;
    private OrderService orderService;
    private TicketService ticketService;

    @Autowired
    CurrentUserRestController(UserService userService, OrderService orderService, TicketService ticketService, TeamService teamService, SeatService seatService) {
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
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        // To prevent 403 errors on this endpoint, we manually handle unauthenticated users, instead of a
        // preauthorize tag.
        if (auth != null) {
            // Get the currently logged in user from the autowired Authentication object.
            UserDetails currentUser = (UserDetails) auth.getPrincipal();
            User user = userService.getUserByUsername(currentUser.getUsername()).get();
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return createResponseEntity(HttpStatus.OK, "Not logged in");
        }
    }

    /**
     * This mapping allows the user to change their password while logged in. This is different from the password reset
     * functionality which works with tokens. Users have to provide both their old and new password, and have to be
     * fully authenticated, meaning that they can't be coming from a "Remember me" session.
     *
     * @param auth The current user
     * @param passwordChangeDTO DTO containing oldPassword and newPassword
     *
     * @return Statusmessage
     */
    @PreAuthorize("isFullyAuthenticated()")
    @RequestMapping(value = "password", method = RequestMethod.PUT)
    public ResponseEntity<?> changeCurrentUserPassword(Authentication auth,
                                                       @RequestBody @Validated PasswordChangeDTO passwordChangeDTO) {
        User currentUser = (User) auth.getPrincipal();

        userService.changePassword(currentUser.getId(), passwordChangeDTO.getOldPassword(),
                passwordChangeDTO.getNewPassword());

        return createResponseEntity(HttpStatus.OK, "Password successfully changed");
    }

    /**
     * Get all the Teams the current user is a member of.
     * @param auth Current Authentication object, automatically taken from the SecurityContext
     * @return A Collection of Teams of which the current User is a member
     */
    @JsonView(View.Team.class)
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/teams", method = RequestMethod.GET)
    public Collection<Team> getCurrentTeams(Authentication auth) {
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        return teamService.getTeamsByUsername(currentUser.getUsername());
    }

    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
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

    @RequestMapping(value = "/seat", method = RequestMethod.GET)
    public List<Seat> getCurrentUserSeat(Authentication auth) {
        User user = (User) auth.getPrincipal();

        return seatService.getSeatsByUsername(user.getUsername());
    }
}
