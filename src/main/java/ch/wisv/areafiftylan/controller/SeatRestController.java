package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.util.Coordinate;
import ch.wisv.areafiftylan.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seats")
public class SeatRestController {

    SeatService seatService;

    @Autowired
    public SeatRestController(SeatService seatService) {
        this.seatService = seatService;
    }

    @RequestMapping(value = "/{x}/{y}", method = RequestMethod.GET)
    Seat getUserByXY(@PathVariable int x, @PathVariable int y) {
        Coordinate coordinate = new Coordinate(x, y);
        return this.seatService.getSeatByCoordinate(coordinate);
    }

    @RequestMapping(method = RequestMethod.GET)
    List<Seat> readUsers() {
        return seatService.getAllSeats();
    }
}
