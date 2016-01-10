package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.SeatGroupDTO;
import ch.wisv.areafiftylan.dto.SeatmapResponse;
import ch.wisv.areafiftylan.model.Seat;

import java.util.Set;

public interface SeatService {

    Seat getSeatByUsername(String username);

    SeatmapResponse getAllSeats();

    SeatmapResponse getSeatGroupByName(String groupname);

    boolean reserveSeat(String groupname, int seatnumber, String username);

    Seat getSeatBySeatGroupAndSeatNumber(String groupName, int seatNumber);

    void addSeats(SeatGroupDTO seatGroupDTO);

    Set<Seat> getSeatsByTeamName(String teamName);
}
