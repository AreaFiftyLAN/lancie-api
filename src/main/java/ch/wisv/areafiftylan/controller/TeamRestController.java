package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.TeamDTO;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;

@RestController
@RequestMapping("/teams")
public class TeamRestController {
    private TeamService teamService;

    @Autowired
    public TeamRestController(TeamService teamService) {
        this.teamService = teamService;
    }

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(@Validated @RequestBody TeamDTO input) {
        Team save = teamService.create(input);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(save.getId()).toUri());

        return new ResponseEntity<>(save, httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET)
    public Collection<Team> readTeams() {
        return teamService.getAllTeams();
    }

    @RequestMapping(method = RequestMethod.GET, value = "{teamId}")
    public Team getTeamById(@PathVariable Long teamId) {
        return this.teamService.getTeamById(teamId).get();
    }

    @RequestMapping(method = RequestMethod.PUT, value = "{teamId}")
    public Team update(@PathVariable Long teamId, @RequestBody TeamDTO input) {
        return this.teamService.update(teamId, input);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "{teamId}")
    public ResponseEntity<?> delete(@PathVariable Long teamId) {
        try {
            this.teamService.delete(teamId);
            return new ResponseEntity<> ("OK", HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>("Not OK", HttpStatus.OK);
        }
    }
}