package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Coordinate;
import ch.wisv.areafiftylan.service.SeatService;
import ch.wisv.areafiftylan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeatRestController {

    SeatService seatService;

    UserService userService;

    @Autowired
    public SeatRestController(SeatService seatService, UserService userService) {
        this.seatService = seatService;
        this.userService = userService;
    }

    @RequestMapping(value = "seats/{x}/{y}", method = RequestMethod.GET)
    Seat getUserByXY(@PathVariable int x, @PathVariable int y) {
        Coordinate coordinate = new Coordinate(x, y);
        return this.seatService.getSeatByCoordinate(coordinate);
    }

    @RequestMapping(value = "/seats", method = RequestMethod.GET)
    List<Seat> readUsers() {
        return seatService.getAllSeats();
    }


    @RequestMapping(value = "/users/{userId}/seat", method = RequestMethod.GET)
    public Seat getSeatByUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);

        return seatService.getSeatByUser(user);
    }
}
