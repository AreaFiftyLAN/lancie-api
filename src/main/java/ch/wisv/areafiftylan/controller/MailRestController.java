package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.MailDTO;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.service.MailService;
import ch.wisv.areafiftylan.service.TeamService;
import ch.wisv.areafiftylan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mail")
public class MailRestController {

    @Autowired
    public MailRestController(MailService mailService, UserService userService, TeamService teamService) {
        this.mailService = mailService;
        this.userService = userService;
        this.teamService = teamService;
    }

    MailService mailService;
    UserService userService;
    TeamService teamService;

    @RequestMapping(value = "/team/{teamId}", method = RequestMethod.POST)
    ResponseEntity<?> sendMailToTeam(@PathVariable Long teamId, @Validated @RequestBody MailDTO mailDTO) {
        Team team = teamService.getTeamById(teamId);
        mailService.sendTemplateMailToTeam(team, mailDTO);

        return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.OK);
    }
}
