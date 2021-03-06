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

package ch.wisv.areafiftylan.seats.service;

import ch.wisv.areafiftylan.seats.model.Seat;
import ch.wisv.areafiftylan.seats.model.SeatGroupDTO;
import ch.wisv.areafiftylan.seats.model.SeatmapResponse;

import java.util.List;

public interface SeatService {

    SeatmapResponse getSeatMap();

    List<Seat> getAllSeats();

    List<Seat> getSeatsByEmail(String email);

    List<Seat> getSeatsByTeamName(String teamName);

    SeatmapResponse getSeatGroupByName(String groupName);

    Seat getSeatBySeatGroupAndSeatNumber(String groupName, int seatNumber);

    boolean reserveSeat(String groupName, int seatNumber, Long ticketId, boolean allowSeatOverride);

    void clearSeat(String groupName, int seatNumber);

    void addSeats(SeatGroupDTO seatGroupDTO);

    void removeSeats(SeatGroupDTO seatGroupDTO);

    void setSeatLocked(String groupName, int seatNumber, boolean locked);

    void setSeatGroupLocked(String groupName, boolean locked);

    void setAllSeatsLock(boolean locked);
}
