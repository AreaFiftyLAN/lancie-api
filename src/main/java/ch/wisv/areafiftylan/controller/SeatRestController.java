package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.SeatGroup;
import ch.wisv.areafiftylan.model.view.View;
import ch.wisv.areafiftylan.service.SeatService;
import ch.wisv.areafiftylan.service.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
        return seatService.getSeatByGroupAndNumber(group, number);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "seats/{group}/{number}", method = RequestMethod.GET, params = "admin")
    Seat getSeatByGroupAndNumberAdmin(@PathVariable String group, @PathVariable int number) {
        return seatService.getSeatByGroupAndNumber(group, number);
    }


    @JsonView(View.Public.class)
    @RequestMapping(value = "seats/{group}", method = RequestMethod.GET)
    SeatGroup getSeatGroupByName(@PathVariable String group) {
        return seatService.getSeatGroupByName(group);
    }


    @RequestMapping(value = "seats/{group}/{number}", method = RequestMethod.POST)
    ResponseEntity<?> reserveSingleSeat(@PathVariable String group, @PathVariable int number, Authentication auth) {
        User user = (User) auth.getPrincipal();
        if (seatService.reserveSeat(group, number, user)) {
            return createResponseEntity(HttpStatus.OK, "Seat successfully reserved");
        } else {
            return createResponseEntity(HttpStatus.CONFLICT, "Seat is already taken");
        }
    }

    @JsonView(View.Public.class)
    @RequestMapping(value = "/seats", method = RequestMethod.GET)
    List<SeatGroup> readUsers() {
        return seatService.getAllSeatGroups();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/seats", method = RequestMethod.POST)
    ResponseEntity<?> addSeatGroup(@RequestBody Map<String, Object> seatDTO) {
        String groupName = String.valueOf(seatDTO.get("groupname"));
        int numberOfSeats = (Integer) seatDTO.get("seats");

        seatService.addSeats(groupName, numberOfSeats);

        return createResponseEntity(HttpStatus.OK, numberOfSeats + " added in group " + groupName);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/users/{userId}/seat", method = RequestMethod.GET)
    public Seat getSeatByUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);

        return seatService.getSeatByUser(user);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/users/current/seat", method = RequestMethod.GET)
    public Seat getCurrentUserSeat(Authentication auth) {
        User user = userService.getUserByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException(auth.getName()));

        return seatService.getSeatByUser(user);
    }
}
