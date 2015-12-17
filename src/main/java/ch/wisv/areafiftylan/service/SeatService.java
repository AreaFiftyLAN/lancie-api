package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.SeatGroup;

import java.util.List;

public interface SeatService {

    Seat getSeatByUser(User user);

    List<Seat> getAllSeats();

    List<SeatGroup> getAllSeatGroups();

    SeatGroup getSeatGroupByName(String groupname);

    boolean reserveSeat(String groupname, int seatnumber, User user);

    Seat getSeatByGroupAndNumber(String groupName, int seatNumber);

    void addSeats(String groupName, int seats);
}
