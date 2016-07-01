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
import ch.wisv.areafiftylan.exception.InvalidTicketException;
import ch.wisv.areafiftylan.seats.model.Seat;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.teams.service.TeamService;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.products.service.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatServiceImpl implements SeatService {

    private SeatRepository seatRepository;
    private TicketRepository ticketRepository;
    private TeamService teamService;

    private static final Object seatReservationLock = new Object();

    @Autowired
    public SeatServiceImpl(SeatRepository seatRepository, TicketRepository ticketRepository, TeamService teamService) {
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
        this.teamService = teamService;
    }

    @Override
    public List<Seat> getSeatsByUsername(String username) {
        return seatRepository.findByTicketOwnerUsernameIgnoreCase(username);

    }

    @Override
    public SeatmapResponse getAllSeats() {
        List<Seat> all = seatRepository.findAll();

        Map<String, List<Seat>> seatMapGroups = all.stream().collect(Collectors.groupingBy(Seat::getSeatGroup));

        return new SeatmapResponse(seatMapGroups);
    }

    @Override
    public SeatmapResponse getSeatGroupByName(String groupname) {
        List<Seat> seatGroup = seatRepository.findBySeatGroup(groupname);

        Map<String, List<Seat>> seatMapResponse = new HashMap<>();
        seatMapResponse.put(groupname, seatGroup);

        return new SeatmapResponse(seatMapResponse);
    }

    @Override
    public boolean reserveSeatForTicket(String groupname, int seatnumber, Long ticketId) {
        Ticket ticket = ticketRepository.findOne(ticketId);

        if(!ticket.isValid()){
            throw new InvalidTicketException("Unable to reserve seat for an invalid Ticket");
        }

        Optional<Seat> previousSeat = seatRepository.findByTicketId(ticketId);

        // We can only reserve one seat at a time, to prevent the extremely unlikely event of simultaneous requests
        synchronized (seatReservationLock) {
            Seat seat = seatRepository.findBySeatGroupAndSeatNumber(groupname, seatnumber);
            previousSeat.ifPresent(s -> s.setTicket(null));
            if (!seat.isTaken()) {
                seat.setTicket(ticket);
                seatRepository.saveAndFlush(seat);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean reserveSeat(String groupname, int seatnumber) {
        // We can only reserve one seat at a time, to prevent the extremely unlikely event of simultaneous requests
        synchronized (seatReservationLock) {
            Seat seat = seatRepository.findBySeatGroupAndSeatNumber(groupname, seatnumber);

            if (!seat.isTaken()) {
                seat.setTaken(true);
                seatRepository.saveAndFlush(seat);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public Seat getSeatBySeatGroupAndSeatNumber(String groupName, int seatNumber) {
        return seatRepository.findBySeatGroupAndSeatNumber(groupName, seatNumber);
    }

    @Override
    public void addSeats(SeatGroupDTO seatGroupDTO) {
        List<Seat> seatList = new ArrayList<>(seatGroupDTO.getNumberOfSeats());

        for (int i = 1; i <= seatGroupDTO.getNumberOfSeats(); i++) {
            seatList.add(new Seat(seatGroupDTO.getSeatGroupName(), i));
        }
        seatRepository.save(seatList);
    }

    @Override
    public List<Seat> getSeatsByTeamName(String teamName) {
        Team team = teamService.getTeamByTeamname(teamName);

        return team.getMembers().stream().
                map(User::getUsername).
                map(seatRepository::findByTicketOwnerUsernameIgnoreCase).
                flatMap(Collection::stream).
                collect(Collectors.toList());
    }

    @Override
    public void clearSeat(String groupName, int seatNumber) {
        Seat seat = seatRepository.findBySeatGroupAndSeatNumber(groupName, seatNumber);
        seat.setTicket(null);
        seatRepository.save(seat);
    }
}
