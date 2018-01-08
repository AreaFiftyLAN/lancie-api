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

package ch.wisv.areafiftylan.seats.controller;

import ch.wisv.areafiftylan.seats.model.SeatGroupDTO;
import ch.wisv.areafiftylan.seats.service.SeatService;
import ch.wisv.areafiftylan.users.model.Role;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.utils.view.View;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("/seats")
public class SeatRestController {

    private final SeatService seatService;

    public SeatRestController(SeatService seatService) {
        this.seatService = seatService;
    }

    /**
     * Get all Seats in the Seatmap.
     *
     * @param admin Boolean for admins to view full data.
     *
     * @return A list of all Seats in the seatmap.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    MappingJacksonValue getAllSeats(@RequestParam(value = "admin", required = false) boolean admin, @AuthenticationPrincipal User user) {
        MappingJacksonValue result = new MappingJacksonValue(seatService.getAllSeats());
        if (!admin || !user.getAuthorities().contains(Role.ROLE_ADMIN)) {
            result.setSerializationView(View.Public.class);
        }
        return result;
    }

    /**
     * Get all Seats in a group.
     *
     * @param group The name of the Seat group.
     * @param admin Boolean for admins to view full data.
     * @return A List of Seats in a group.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{group}")
    MappingJacksonValue getSeatGroupByName(@PathVariable String group, @RequestParam(value = "admin", required = false) boolean admin, @AuthenticationPrincipal User user) {
        MappingJacksonValue result = new MappingJacksonValue(seatService.getSeatGroupByName(group));
        if (!admin || !user.getAuthorities().contains(Role.ROLE_ADMIN)) {
            result.setSerializationView(View.Public.class);
        }
        return result;
    }

    /**
     * Get a Seat based on seatGroup and seatNumber
     *
     * @param group  Group of the Seat
     * @param number Number in the group of the Seat
     * @param admin Boolean for admins to view full data.
     *
     * @return The seat at the given location
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{group}/{number}")
    MappingJacksonValue getSeatByGroupAndNumber(@PathVariable String group, @PathVariable int number, @RequestParam(value = "admin", required = false) boolean admin, @AuthenticationPrincipal User user) {
        MappingJacksonValue result = new MappingJacksonValue(seatService.getSeatBySeatGroupAndSeatNumber(group, number));
        if (!admin || !user.getAuthorities().contains(Role.ROLE_ADMIN)) {
            result.setSerializationView(View.Public.class);
        }
        return result;
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
    ResponseEntity<?> reserveSingleSeat(@PathVariable String group, @PathVariable Integer number,
                                        @PathVariable Long ticketId, @AuthenticationPrincipal User user) {
        if (seatService.reserveSeat(group, number, ticketId, user.getAuthorities().contains(Role.ROLE_ADMIN))) {
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
        seatService.clearSeat(group, number);
        return createResponseEntity(HttpStatus.OK, "Seat successfully reserved");
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

    /**
     * Remove Seats from a SeatGroup, or remove the SeatGroup altogether.
     * @param seatGroupDTO DTO with the name of the group, and the amount of seats to remove from that group.
     *                     Removing all seats from a group removes that group.
     * @return Status message indicating the result
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    ResponseEntity<?> removeSeatGroup(@RequestBody @Validated SeatGroupDTO seatGroupDTO) {
        seatService.removeSeats(seatGroupDTO);
        return createResponseEntity(HttpStatus.OK,
                seatGroupDTO.getNumberOfSeats() + " removed in group " + seatGroupDTO.getSeatGroupName());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/lock")
    ResponseEntity<?> setAllSeatsLock(@RequestBody Boolean lock) {
        seatService.setAllSeatsLock(lock);
        return createResponseEntity(HttpStatus.OK, lock ? "All seats successfully locked." : "All seats successfully unlocked.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/lock/{group}")
    ResponseEntity<?> setSeatGroupLock(@PathVariable String group, @RequestBody Boolean lock) {
        seatService.setSeatGroupLocked(group, lock);
        return createResponseEntity(HttpStatus.OK, lock ? "SeatGroup successfully locked." : "SeatGroup successfully unlocked.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/lock/{group}/{number}")
    ResponseEntity<?> setSeatLock(@PathVariable String group, @PathVariable int number, @RequestBody Boolean lock) {
        seatService.setSeatLocked(group, number, lock);
        return createResponseEntity(HttpStatus.OK, lock ? "Seat successfully locked." : "Seat successfully unlocked.");
    }

}
