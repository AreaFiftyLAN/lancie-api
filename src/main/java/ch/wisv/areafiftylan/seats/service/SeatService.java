/*
 * Copyright (c) 2016  W.I.S.V. 'Christiaan Huygens'
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

import ch.wisv.areafiftylan.seats.model.SeatGroupDTO;
import ch.wisv.areafiftylan.seats.model.SeatmapResponse;
import ch.wisv.areafiftylan.seats.model.Seat;

import java.util.List;

public interface SeatService {

    List<Seat> getSeatsByUsername(String username);

    SeatmapResponse getAllSeats();

    SeatmapResponse getSeatGroupByName(String groupname);

    boolean reserveSeatForTicket(String groupname, int seatnumber, Long ticketId);

    boolean reserveSeat(String groupname, int seatnumber);

    Seat getSeatBySeatGroupAndSeatNumber(String groupName, int seatNumber);

    void addSeats(SeatGroupDTO seatGroupDTO);

    List<Seat> getSeatsByTeamName(String teamName);

    void clearSeat(String groupName, int seatNumber);
}
