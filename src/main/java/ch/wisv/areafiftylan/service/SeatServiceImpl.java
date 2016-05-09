package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.SeatGroupDTO;
import ch.wisv.areafiftylan.dto.SeatmapResponse;
import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.repository.SeatRepository;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
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
        List<Seat> seatGroup = seatRepository.findBySeatGroupIgnoreCase(groupname);

        Map<String, List<Seat>> seatMapResponse = new HashMap<>();
        seatMapResponse.put(seatGroup.get(0).getSeatGroup(), seatGroup);

        return new SeatmapResponse(seatMapResponse);
    }

    @Override
    public boolean reserveSeatForTicket(String groupname, int seatnumber, Long ticketId) {
        Ticket ticket = ticketRepository.findOne(ticketId);

        Optional<Seat> previousSeat = seatRepository.findByTicketId(ticketId);

        // We can only reserve one seat at a time, to prevent the extremely unlikely event of simultaneous requests
        synchronized (seatReservationLock) {
            Seat seat = seatRepository.findBySeatGroupIgnoreCaseAndSeatNumber(groupname, seatnumber);
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
            Seat seat = seatRepository.findBySeatGroupIgnoreCaseAndSeatNumber(groupname, seatnumber);

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
        return seatRepository.findBySeatGroupIgnoreCaseAndSeatNumber(groupName, seatNumber);
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
        Seat seat = seatRepository.findBySeatGroupIgnoreCaseAndSeatNumber(groupName, seatNumber);
        seat.setTicket(null);
    }
}
