package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.SeatGroupDTO;
import ch.wisv.areafiftylan.dto.SeatmapResponse;
import ch.wisv.areafiftylan.exception.SeatNotFoundException;
import ch.wisv.areafiftylan.exception.TeamNotFoundException;
import ch.wisv.areafiftylan.exception.UserNotFoundException;
import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.repository.SeatRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SeatServiceImpl implements SeatService {

    SeatRespository seatRespository;
    UserService userService;
    TeamService teamService;

    @Autowired
    public SeatServiceImpl(SeatRespository seatRespository, UserService userService, TeamService teamService) {
        this.seatRespository = seatRespository;
        this.userService = userService;
        this.teamService = teamService;
    }

    @Override
    public Seat getSeatByUsername(String username) {
        return seatRespository.findByUserUsername(username)
                .orElseThrow(() -> new SeatNotFoundException("User " + username + " doesn't have a seat"));
    }

    @Override
    public SeatmapResponse getAllSeats() {
        List<Seat> all = seatRespository.findAll();

        Map<String, List<Seat>> seatMapResponse = new HashMap<>();

        for (Seat seat : all) {
            if (seatMapResponse.containsKey(seat.getSeatGroup())) {
                seatMapResponse.get(seat.getSeatGroup()).add(seat);
            } else {
                List<Seat> seats = new ArrayList<>();
                seats.add(seat);
                seatMapResponse.put(seat.getSeatGroup(), seats);
            }
        }

        return new SeatmapResponse(seatMapResponse);
    }

    @Override
    public SeatmapResponse getSeatGroupByName(String groupname) {
        List<Seat> seatGroup = seatRespository.findBySeatGroup(groupname);

        Map<String, List<Seat>> seatMapResponse = new HashMap<>();
        seatMapResponse.put(groupname, seatGroup);

        return new SeatmapResponse(seatMapResponse);
    }

    @Override
    public boolean reserveSeat(String groupname, int seatnumber, String username) {
        User user = userService.getUserByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
        Seat seat = seatRespository.findBySeatGroupSeatNumber(groupname, seatnumber);
        if (!seat.isTaken()) {
            seat.setUser(user);
            seatRespository.saveAndFlush(seat);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Seat getSeatBySeatGroupAndSeatNumber(String groupName, int seatNumber) {
        return seatRespository.findBySeatGroupSeatNumber(groupName, seatNumber);
    }

    @Override
    public void addSeats(SeatGroupDTO seatGroupDTO) {
        List<Seat> seatList = new ArrayList<>(seatGroupDTO.getNumberOfSeats());

        for (int i = 1; i <= seatGroupDTO.getNumberOfSeats(); i++) {
            seatList.add(new Seat(seatGroupDTO.getSeatGroupName(), i));
        }

        seatRespository.save(seatList);
    }

    @Override
    public Set<Seat> getSeatsByTeamName(String teamName) {
        Set<Seat> seats = new HashSet<>();
        Team team = teamService.getTeamByTeamname(teamName).orElseThrow(() -> new TeamNotFoundException(teamName));

        for (User user : team.getMembers()) {
            Optional<Seat> seat = seatRespository.findByUserUsername(user.getUsername());
            if (seat.isPresent()) {
                seats.add(seat.get());
            }
        }

        return seats;
    }
}
