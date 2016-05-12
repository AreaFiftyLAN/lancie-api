package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.MailDTO;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.MailService;
import ch.wisv.areafiftylan.service.TeamService;
import ch.wisv.areafiftylan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/mail")
public class MailRestController {

    @Autowired
    public MailRestController(MailService mailService, UserService userService, TeamService teamService) {
        this.mailService = mailService;
        this.userService = userService;
        this.teamService = teamService;
    }

    private MailService mailService;
    private UserService userService;
    private TeamService teamService;

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

    @RequestMapping(value = "/users/all/YESREALLY", method = RequestMethod.POST)
    ResponseEntity<?> sendMailToAll(@Validated @RequestBody MailDTO mailDTO) {
        mailService.sendTemplateMailToAll(userService.getAllUsers(), mailDTO);
        return createResponseEntity(HttpStatus.OK, "Mail successfully sent");
    }
}
