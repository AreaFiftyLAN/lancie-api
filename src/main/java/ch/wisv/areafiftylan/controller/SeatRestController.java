package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.SeatGroupDTO;
import ch.wisv.areafiftylan.dto.SeatmapResponse;
import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.view.View;
import ch.wisv.areafiftylan.service.SeatService;
import ch.wisv.areafiftylan.service.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

@RestController
public class SeatRestController {

    SeatService seatService;

    UserService userService;

    @Autowired
    public SeatRestController(SeatService seatService, UserService userService) {
        this.seatService = seatService;
        this.userService = userService;
    }

    @JsonView(View.Public.class)
    @RequestMapping(value = "seats/{group}/{number}", method = RequestMethod.GET)
    Seat getSeatByGroupAndNumber(@PathVariable String group, @PathVariable int number) {
        return seatService.getSeatBySeatGroupAndSeatNumber(group, number);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "seats/{group}/{number}", method = RequestMethod.GET, params = "admin")
    Seat getSeatByGroupAndNumberAdmin(@PathVariable String group, @PathVariable int number) {
        return seatService.getSeatBySeatGroupAndSeatNumber(group, number);
    }


    @JsonView(View.Public.class)
    @RequestMapping(value = "seats/{group}", method = RequestMethod.GET)
    SeatmapResponse getSeatGroupByName(@PathVariable String group) {
        return seatService.getSeatGroupByName(group);
    }


    @RequestMapping(value = "seats/{group}/{number}", method = RequestMethod.POST)
    ResponseEntity<?> reserveSingleSeat(@PathVariable String group, @PathVariable int number, Authentication auth) {
        User user = (User) auth.getPrincipal();
        if (seatService.reserveSeat(group, number, user.getUsername())) {
            return createResponseEntity(HttpStatus.OK, "Seat successfully reserved");
        } else {
            return createResponseEntity(HttpStatus.CONFLICT, "Seat is already taken");
        }
    }

    @JsonView(View.Public.class)
    @RequestMapping(value = "/seats", method = RequestMethod.GET)
    SeatmapResponse getAllSeats() {
        return seatService.getAllSeats();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/seats", method = RequestMethod.POST)
    ResponseEntity<?> addSeatGroup(@RequestBody @Validated SeatGroupDTO seatGroupDTO) {
        seatService.addSeats(seatGroupDTO);

        return createResponseEntity(HttpStatus.OK,
                seatGroupDTO.getNumberOfSeats() + " added in group " + seatGroupDTO.getSeatGroupName());
    }


    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/users/{userId}/seat", method = RequestMethod.GET)
    public Seat getSeatByUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);

        return seatService.getSeatByUsername(user.getUsername());
    }
}
