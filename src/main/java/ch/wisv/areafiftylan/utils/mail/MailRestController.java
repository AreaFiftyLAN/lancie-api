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

package ch.wisv.areafiftylan.utils.mail;

import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.service.TicketService;
import ch.wisv.areafiftylan.seats.model.Seat;
import ch.wisv.areafiftylan.seats.service.SeatService;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.teams.service.TeamService;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.stream.Collectors;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/mail")
public class MailRestController {


    private MailService mailService;
    private UserService userService;
    private TeamService teamService;
    private TicketService ticketService;
    private SeatService seatService;

    @Autowired
    public MailRestController(MailService mailService, UserService userService, TeamService teamService, TicketService ticketService, SeatService seatService) {
        this.mailService = mailService;
        this.userService = userService;
        this.teamService = teamService;
        this.ticketService = ticketService;
        this.seatService = seatService;
    }

    @RequestMapping(value = "/user/{userId}", method = RequestMethod.POST)
    ResponseEntity<?> sendMailToUser(@PathVariable Long userId, @Validated @RequestBody MailDTO mailDTO) {
        User user = userService.getUserById(userId);

        mailService.sendTemplateMailToUser(user, mailDTO);

        return createResponseEntity(HttpStatus.OK, "Mail successfully sent");

    }

    @RequestMapping(value = "/team/{teamId}", method = RequestMethod.POST)
    ResponseEntity<?> sendMailToTeam(@PathVariable Long teamId, @Validated @RequestBody MailDTO mailDTO) {
        Team team = teamService.getTeamById(teamId);
        mailService.sendTemplateMailToTeam(team, mailDTO);

        return createResponseEntity(HttpStatus.OK, "Mail successfully sent");
    }

    @RequestMapping(value = "/noticket", method = RequestMethod.POST)
    ResponseEntity<?> sendMailToUserWithoutTicket(@Validated @RequestBody MailDTO mailDTO) {
        Collection<User> users = ticketService.getAllTickets().stream()
                .map(t -> t.getOwner())
                .distinct()
                .collect(Collectors.toList());

        Collection<User> usersWithoutTicket = userService.getAllUsers().stream()
                .filter(u -> !users.contains(u))
                .collect(Collectors.toList());

        mailService.sendTemplateMailToAll(usersWithoutTicket, mailDTO);

        return createResponseEntity(HttpStatus.OK, "Mail successfully sent");
    }

    @RequestMapping(value = "/ticket", method = RequestMethod.POST)
    ResponseEntity<?> sendMailToUserWithTicket(@Validated @RequestBody MailDTO mailDTO) {
        Collection<User> users = ticketService.getAllTickets().stream()
                .map(t -> t.getOwner())
                .distinct()
                .collect(Collectors.toList());

        mailService.sendTemplateMailToAll(users, mailDTO);

        return createResponseEntity(HttpStatus.OK, "Mail successfully sent");
    }

    @RequestMapping(value = "/pickup", method = RequestMethod.POST)
    ResponseEntity<?> sendMailToUserWithPickup(@Validated @RequestBody MailDTO mailDTO) {
        Collection<User> users = ticketService.getAllTicketsWithTransport().stream()
                .map(Ticket::getOwner)
                .collect(Collectors.toList());

        mailService.sendTemplateMailToAll(users, mailDTO);

        return createResponseEntity(HttpStatus.OK, "Mail was successfully sent");
    }

    @RequestMapping(value = "/user/{seatgroup}", method = RequestMethod.POST)
    ResponseEntity<?> sendMailToSeatGroup(@RequestParam String seatGroup, @Validated @RequestBody MailDTO mailDTO) {
        Collection<User> users = seatService.getSeatsBySeatGroup(seatGroup).stream()
                .filter(s -> s.isTaken())
                .map(Seat::getUser)
                .collect(Collectors.toList());

        mailService.sendTemplateMailToAll(users, mailDTO);

        return createResponseEntity(HttpStatus.OK, "Mail was successfully sent");
    }

    @RequestMapping(value = "/users/all/YESREALLY", method = RequestMethod.POST)
    ResponseEntity<?> sendMailToAll(@Validated @RequestBody MailDTO mailDTO) {
        mailService.sendTemplateMailToAll(userService.getAllUsers(), mailDTO);
        return createResponseEntity(HttpStatus.OK, "Mail successfully sent");
    }
}