package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.TeamDTO;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

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
        httpHeaders.setLocation(
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(save.getId()).toUri());

        return createResponseEntity(HttpStatus.CREATED, httpHeaders,
                "Team succesfully created at " + httpHeaders.getLocation(), save);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(method = RequestMethod.GET)
    public Collection<Team> readTeams() {
        return teamService.getAllTeams();
    }

    @PreAuthorize("@currentUserServiceImpl.canAccessTeam(principal, #teamId)")
    @RequestMapping(method = RequestMethod.GET, value = "/{teamId}")
    public Team getTeamById(@PathVariable Long teamId) {
        return this.teamService.getTeamById(teamId).get();
    }

    @PreAuthorize("@currentUserServiceImpl.canEditTeam(principal, #teamId)")
    @RequestMapping(method = RequestMethod.PUT, value = "/{teamId}")
    public Team update(@PathVariable Long teamId, @RequestBody TeamDTO input) {
        return this.teamService.update(teamId, input);
    }

    @PreAuthorize("@currentUserServiceImpl.canEditTeam(principal, #teamId)")
    @RequestMapping(method = RequestMethod.DELETE, value = "{teamId}")
    public ResponseEntity<?> delete(@PathVariable Long teamId) {
        try {
            this.teamService.delete(teamId);
            return createResponseEntity(HttpStatus.OK, "Deleted team with " + teamId);
        } catch (EmptyResultDataAccessException e) {
            return createResponseEntity(HttpStatus.BAD_REQUEST, "Team can not be deleted.");
        }
    }
}