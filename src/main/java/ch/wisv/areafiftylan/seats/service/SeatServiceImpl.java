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

import ch.wisv.areafiftylan.exception.InvalidTicketException;
import ch.wisv.areafiftylan.exception.SeatNotFoundException;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.service.TicketService;
import ch.wisv.areafiftylan.seats.model.Seat;
import ch.wisv.areafiftylan.seats.model.SeatGroupDTO;
import ch.wisv.areafiftylan.seats.model.SeatmapResponse;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.teams.service.TeamService;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.utils.mail.MailService;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final TeamService teamService;
    private final TicketService ticketService;
    private final MailService mailService;

    @Autowired
    public SeatServiceImpl(SeatRepository seatRepository, TeamService teamService, TicketService ticketService, MailService mailService) {
        this.seatRepository = seatRepository;
        this.teamService = teamService;
        this.ticketService = ticketService;
        this.mailService = mailService;
    }

    @Override
    public SeatmapResponse getAllSeats() {
        return new SeatmapResponse(seatRepository.findAll().
                stream().
                collect(Collectors.groupingBy(Seat::getSeatGroup)));
    }

    @Override
    public List<Seat> getSeatsByEmail(String email) {
        return seatRepository.findByTicketOwnerEmailIgnoreCase(email);

    }

    @Override
    public List<Seat> getSeatsByTeamName(String teamName) {
        Team team = teamService.getTeamByTeamname(teamName);

        return team.getMembers().stream().
                map(User::getEmail).
                map(seatRepository::findByTicketOwnerEmailIgnoreCase).
                flatMap(Collection::stream).
                collect(Collectors.toList());
    }

    @Override
    public SeatmapResponse getSeatGroupByName(String groupName) {
        List<Seat> seatGroup = seatRepository.findBySeatGroup(groupName);

        Map<String, List<Seat>> seatMapResponse = new HashMap<>();
        seatMapResponse.put(groupName, seatGroup);

        return new SeatmapResponse(seatMapResponse);
    }

    @Override
    public Seat getSeatBySeatGroupAndSeatNumber(String groupName, int seatNumber) {
        return seatRepository.findBySeatGroupAndSeatNumber(groupName, seatNumber)
                .orElseThrow(SeatNotFoundException::new);
    }

    @Override
    @Synchronized
    public boolean reserveSeat(String groupName, int seatNumber, Long ticketId, boolean allowSeatOverride) {
        Seat seat = getSeatBySeatGroupAndSeatNumber(groupName, seatNumber);
        Ticket ticket = null;

        if (!allowSeatOverride && (seat.isTaken() || seat.isLocked() || ticketId == null)) {
            return false;
        }

        if (seat.isTaken() && seat.getTicket().getOwner() != null) {
            mailService.sendSeatOverrideMail(seat.getTicket().getOwner());
        }
        if (ticketId != null) {
            seatRepository.findByTicketId(ticketId).ifPresent(previousSeat -> previousSeat.setTicket(null));
            ticket = ticketService.getTicketById(ticketId);
            if (!ticket.isValid()) {
                throw new InvalidTicketException("Unable to reserve seat for an invalid Ticket");
            }
        }
        seat.setTicket(ticket);
        seatRepository.saveAndFlush(seat);
        return true;
    }

    @Override
    public void clearSeat(String groupName, int seatNumber) {
        reserveSeat(groupName, seatNumber, null, true);
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
    public void removeSeats(SeatGroupDTO seatGroupDTO) {
        String seatGroupName = seatGroupDTO.getSeatGroupName();
        int seatsInSeatGroup = getSeatGroupByName(seatGroupName).getSeatmap().get(seatGroupName).size();
        int seatsToRemove = seatGroupDTO.getNumberOfSeats();
        int lowestSeatToRemove = Math.max(1, seatsInSeatGroup - seatsToRemove + 1);

        // We want to start removing seats with the highest numbers first
        for (int i = seatsInSeatGroup; i >= lowestSeatToRemove; i--) {
            clearSeat(seatGroupName, i);
            Seat seat = getSeatBySeatGroupAndSeatNumber(seatGroupName, i);
            seatRepository.delete(seat);
        }
    }

    @Override
    public void setSeatLocked(String groupName, int seatNumber, boolean locked) {
        Seat seat = getSeatBySeatGroupAndSeatNumber(groupName, seatNumber);
        seat.setLocked(locked);
        seatRepository.save(seat);
    }

    @Override
    public void setSeatGroupLocked(String groupName, boolean locked) {
        List<Seat> seatGroup = seatRepository.findBySeatGroup(groupName);
        seatGroup.forEach(seat -> seat.setLocked(locked));
        seatRepository.save(seatGroup);
    }

    @Override
    public void setAllSeatsLock(boolean locked) {
        List<Seat> seats = seatRepository.findAll();
        seats.forEach(seat -> seat.setLocked(locked));
        seatRepository.save(seats);
    }
}
