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

package ch.wisv.areafiftylan.utils.mail;

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

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("/mail")
public class MailRestController {

    @Autowired
    public MailRestController(MailService mailService, UserService userService, TeamService teamService) {
        this.mailService = mailService;
        this.userService = userService;
        this.teamService = teamService;
    }

    private final MailService mailService;
    private final UserService userService;
    private final TeamService teamService;

    @PostMapping("/contact")
    ResponseEntity<?> sendContactForm(@Validated @RequestBody ContactMailDTO mailDTO) {
        mailService.sendContactMail(mailDTO.getSender(), mailDTO.getSubject(), mailDTO.getMessage());

        return createResponseEntity(HttpStatus.OK, "Mail successfully sent");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/user/{userId}")
    ResponseEntity<?> sendMailToUser(@PathVariable Long userId, @Validated @RequestBody MailDTO mailDTO) {
        User user = userService.getUserById(userId);

        mailService.sendTemplateMailToUser(user, mailDTO);

        return createResponseEntity(HttpStatus.OK, "Mail successfully sent");

    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/team/{teamId}")
    ResponseEntity<?> sendMailToTeam(@PathVariable Long teamId, @Validated @RequestBody MailDTO mailDTO) {
        Team team = teamService.getTeamById(teamId);
        mailService.sendTemplateMailToTeam(team, mailDTO);

        return createResponseEntity(HttpStatus.OK, "Mail successfully sent");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/all/YESREALLY")
    ResponseEntity<?> sendMailToAll(@Validated @RequestBody MailDTO mailDTO) {
        mailService.sendTemplateMailToAll(userService.getAllUsers(), mailDTO);
        return createResponseEntity(HttpStatus.OK, "Mail successfully sent");
    }
}
