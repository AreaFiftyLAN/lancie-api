package ch.wisv.areafiftylan.utils;

import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.service.TicketService;
import ch.wisv.areafiftylan.seats.model.Seat;
import ch.wisv.areafiftylan.seats.service.SeatService;
import ch.wisv.areafiftylan.teams.model.TeamExportDTO;
import ch.wisv.areafiftylan.teams.service.TeamService;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.model.UserExportDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * This controller creates an export endpoint, meant for manual single-use export for use in different system.
 * No production functionality should depend on this endpoint as it may change based on different requirements
 */
@RestController
@RequestMapping("/export")
@PreAuthorize("hasRole('ADMIN')")
public class ExportController {
    private final SeatService seatService;
    private final TeamService teamService;
    private final TicketService ticketService;

    public ExportController(SeatService seatService, TeamService teamService, TicketService ticketService) {
        this.seatService = seatService;
        this.teamService = teamService;
        this.ticketService = ticketService;
    }

    @GetMapping
    public Map<String, ?> exportUsers() {
        Map<String, Object> exportMap = new HashMap<>();
        Map<Long, List<Seat>> seatMap = seatService.getAllSeats().stream()
                .filter(Seat::isTaken)
                .collect(Collectors.groupingBy(seat -> seat.getUser().getId()));

        exportMap.put("users",
                ticketService.getAllTickets().stream()
                        .filter(Ticket::isValid)
                        .map(Ticket::getOwner)
                        .map(user -> {
                            String displayName = user.getProfile() != null ? user.getProfile().getDisplayName() : null;

                            List<String> seats = Collections.emptyList();
                            if (!seatMap.isEmpty()) {
                                seatMap.get(user.getId()).forEach(seat -> seats.add(seat.toString()));
                            }

                            return new UserExportDTO(user.getEmail(), user.getPassword(),
                                    displayName, user.getId(), seats);
                        })
                        .collect(Collectors.toList()));

        exportMap.put("teams",
                teamService.getAllTeams().stream()
                        .map(team -> new TeamExportDTO(team.getTeamName(), team.getCaptain().getId(),
                                team.getMembers().stream()
                                        .map(User::getId).collect(Collectors.toList()))));

        return exportMap;
    }
}
