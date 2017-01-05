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

package ch.wisv.areafiftylan.seats.controller;

import ch.wisv.areafiftylan.seats.model.Seat;
import ch.wisv.areafiftylan.seats.model.SeatGroupDTO;
import ch.wisv.areafiftylan.seats.model.SeatmapResponse;
import ch.wisv.areafiftylan.seats.service.SeatService;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.service.UserService;
import ch.wisv.areafiftylan.utils.view.View;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("/seats")
public class SeatRestController {

    private final SeatService seatService;
    private final UserService userService;

    @Autowired
    public SeatRestController(SeatService seatService, UserService userService) {
        this.seatService = seatService;
        this.userService = userService;
    }

    /**
     * Get a Seat based on seatGroup and seatNumber
     *
     * @param group  Group of the Seat
     * @param number Number in the group of the Seat
     *
     * @return The seat at the given location
     */
    @JsonView(View.Public.class)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{group}/{number}")
    Seat getSeatByGroupAndNumber(@PathVariable String group, @PathVariable int number) {
        return seatService.getSeatBySeatGroupAndSeatNumber(group, number);
    }

    /**
     * Get a Seat based on seatGroup and seatNumber, with full User details. Only available to Admins
     *
     * @param group  Group of the Seat
     * @param number Number in the group of the Seat
     *
     * @return The seat at the given location
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/{group}/{number}", params = "admin")
    Seat getSeatByGroupAndNumberAdmin(@PathVariable String group, @PathVariable int number) {
        return seatService.getSeatBySeatGroupAndSeatNumber(group, number);
    }

    /**
     * Get all Seats in a group
     *
     * @param group The group you want the seats of.
     *
     * @return A List of Seats in a group.
     */
    @JsonView(View.Public.class)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{group}")
    SeatmapResponse getSeatGroupByName(@PathVariable String group) {
        return seatService.getSeatGroupByName(group);
    }

    /**
     * Get all Seats in a group, with full User details.
     *
     * @param group The group you want the seats of.
     *
     * @return A List of Seats in a group.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/{group}", params = "admin")
    SeatmapResponse getSeatGroupByNameAdminView(@PathVariable String group) {
        return seatService.getSeatGroupByName(group);
    }

    /**
     * Reserve a Seat at a given location. This can be done for yourself, an Admin or a member of a Team you're the
     * captain of.
     *
     * @param group    The Seatgroup the Seat is in.
     * @param number   The number of the Seat in the Seatgroup.
     * @param ticketId The ticketId of the User's Ticket.
     *
     * @return Status message indicating the result.
     */
    @PreAuthorize("@currentUserServiceImpl.canReserveSeat(principal, #ticketId)")
    @PostMapping("/{group}/{number}/{ticketId}")
    ResponseEntity<?> reserveSingleSeat(@PathVariable String group, @PathVariable Integer number, @PathVariable Long ticketId) {
        if (seatService.reserveSeatForTicket(group, number, ticketId)) {
            return createResponseEntity(HttpStatus.OK, "Seat successfully reserved");
        } else {
            return createResponseEntity(HttpStatus.CONFLICT, "Seat is already taken");
        }
    }

    /**
     * Reserve a seat without assigning a User to it. Can be used for Group reservations. Can only be done by Admins.
     *
     * @param group    The Seatgroup the Seat is in.
     * @param number   The number of the Seat in the Seatgroup.
     *
     * @return Status message indicating the result
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{group}/{number}")
    ResponseEntity<?> reserveSingleSeat(@PathVariable String group, @PathVariable Integer number) {
        if (seatService.reserveSeat(group, number)) {
            return createResponseEntity(HttpStatus.OK, "Seat successfully reserved");
        } else {
            return createResponseEntity(HttpStatus.CONFLICT, "Seat is already taken");
        }
    }

    /**
     * Remove a User from a specific Seat
     *
     * @param group  Groupname of the seat
     * @param number Number of the seat
     *
     * @return Status message of the result
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{group}/{number}")
    ResponseEntity<?> clearSeat(@PathVariable String group, @PathVariable int number) {

        seatService.clearSeat(group, number);
        return createResponseEntity(HttpStatus.OK, "Seat successfully cleared");
    }

    /**
     * Get all seats in the Seatmap
     *
     * @return A list of all Seats in the seatmap.
     */
    @JsonView(View.Public.class)
    @GetMapping
    SeatmapResponse getAllSeats() {
        return seatService.getAllSeats();
    }

    /**
     * Get all seats in the Seatmap, with full User details.
     *
     * @return A list of all Seats in the seatmap.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(params = "admin")
    SeatmapResponse getAllSeatsAdminView() {
        return seatService.getAllSeats();
    }

    /**
     * Add a Seat group to the Seatmap.
     *
     * @param seatGroupDTO DTO containing the name of the group and the amount of Seats in the group
     *
     * @return Status message indicating the result
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    ResponseEntity<?> addSeatGroup(@RequestBody @Validated SeatGroupDTO seatGroupDTO) {
        seatService.addSeats(seatGroupDTO);

        return createResponseEntity(HttpStatus.OK,
                seatGroupDTO.getNumberOfSeats() + " added in group " + seatGroupDTO.getSeatGroupName());
    }
}
