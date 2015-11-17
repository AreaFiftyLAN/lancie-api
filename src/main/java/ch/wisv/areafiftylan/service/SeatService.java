package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Coordinate;

import java.util.List;

public interface SeatService {

    Seat getSeatByUser(User user);

    List<Seat> getAllSeats();

    Seat getSeatByCoordinate(Coordinate coordinate);

    Seat reserveSeat(Coordinate coordinate, User user);

    Seat getSeatByGroupAndNumber(String groupName, int seatNumber);

    void addSeats(String groupName, int seats);
}
