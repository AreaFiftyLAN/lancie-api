package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/teams")
public class TeamRestController {
    private TeamService teamService;

    @Autowired
    public TeamRestController(TeamService teamService) {
        this.teamService = teamService;
    }

    @RequestMapping(method = RequestMethod.GET)
    Collection<Team> readUsers() {
        return teamService.getAllTeams();
    }
}
