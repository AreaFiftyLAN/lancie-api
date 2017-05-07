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
    public List<Seat> getSeatsByEmail(String email) {
        return seatRepository.findByTicketOwnerEmailIgnoreCase(email);

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
    public boolean reserveSeatForTicket(String seatGroup, int seatNumber, Long ticketId) {
        Seat seat = getSeatBySeatGroupAndSeatNumber(seatGroup, seatNumber);
        if (seat.isTaken()) {
            return false;
        } else {
            reserveSeat(seat, ticketId);
            return true;
        }
    }

    @Override
    public void reserveSeatForAdmin(String seatGroup, int seatNumber) {
        Seat seat = getSeatBySeatGroupAndSeatNumber(seatGroup, seatNumber);
        reserveSeat(seat, null);
    }

    /**
     * Reserves a Seat, and sets the Ticket to ticketId's Ticket.
     * Also frees any previous Seat belonging to the Ticket.
     * This method ignores any current user assigned to the seat, that logic must be done before calling this class.
     * If any user was assigned this seat before, send an email to let them know their seat was overridden.
     * This method is synchronized to prevent the extremely unlikely event of simultaneous requests.
     *
     * @param seat The Seat to reserve.
     * @param ticketId The Id of the Ticket to be set to the Seat.
     */
    @Synchronized
    private void reserveSeat(Seat seat, Long ticketId) {
        if (ticketId != null) {
            seatRepository.findByTicketId(ticketId).ifPresent(previousSeat -> previousSeat.setTicket(null));
            Ticket ticket = ticketService.getTicketById(ticketId);
            if (!ticket.isValid()) {
                throw new InvalidTicketException("Unable to reserve seat for an invalid Ticket");
            }
            if (seat.getTicket() != null && seat.getTicket().getOwner() != null) {
                mailService.sendSeatOverrideMail(seat.getTicket().getOwner());
            }
            seat.setTicket(ticket);
        } else {
            seat.setTicket(null);
        }
        seat.setTaken(true);
        seatRepository.saveAndFlush(seat);
    }

    @Override
    public Seat getSeatBySeatGroupAndSeatNumber(String groupName, int seatNumber) {
        return seatRepository.findBySeatGroupAndSeatNumber(groupName, seatNumber)
                .orElseThrow(SeatNotFoundException::new);
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
                map(User::getEmail).
                map(seatRepository::findByTicketOwnerEmailIgnoreCase).
                flatMap(Collection::stream).
                collect(Collectors.toList());
    }

    @Override
    public void clearSeat(String groupName, int seatNumber) {
        Seat seat = getSeatBySeatGroupAndSeatNumber(groupName, seatNumber);
        seat.setTicket(null);
        seatRepository.save(seat);
    }
}
