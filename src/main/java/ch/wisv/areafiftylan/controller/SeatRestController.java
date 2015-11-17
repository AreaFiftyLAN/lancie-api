package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.SeatService;
import ch.wisv.areafiftylan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

    //TODO: Add JSON View (After team-update merge)
    @RequestMapping(value = "seats/{group}/{number}", method = RequestMethod.GET)
    Seat getSeatByGroupAndNumber(@PathVariable String group, @PathVariable int number) {
        Seat seat = seatService.getSeatByGroupAndNumber(group, number);

        return seat;
    }

    //TODO: JSON View
    @RequestMapping(value = "/seats", method = RequestMethod.GET)
    List<Seat> readUsers() {
        return seatService.getAllSeats();
    }


    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/seats", method = RequestMethod.POST)
    ResponseEntity<?> addSeatGroup(Map<String, Object> seatDTO) {
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
    public Seat getSeatByUser(Authentication auth) {
        User user = userService.getUserByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException(auth.getName()));

        return seatService.getSeatByUser(user);
    }
}
