package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.EventDTO;
import ch.wisv.areafiftylan.model.Event;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.view.View;
import ch.wisv.areafiftylan.service.EventService;
import ch.wisv.areafiftylan.service.TeamService;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

/**
 * Created by Sille Kamoen on 27-3-16.
 */
@RestController
@RequestMapping(value = "/events")
public class EventRestController {

    private EventService eventService;
    private TeamService teamService;

    @Autowired
    public EventRestController(EventService eventService, TeamService teamService) {
        this.eventService = eventService;
        this.teamService = teamService;
    }

    /**
     * Add a new WebEvent
     *
     * @param eventDTO EventDTO containing all info about the WebEvent. Validated for null fields
     *
     * @return Message containing the location of the created WebEvent
     */
    @RequestMapping(method = RequestMethod.POST)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addEvent(@Validated @RequestBody EventDTO eventDTO) {
        Event event = eventService.addEvent(eventDTO);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(event.getId()).toUri());

        return createResponseEntity(HttpStatus.CREATED, httpHeaders,
                "Event successfully created at " + httpHeaders.getLocation(), event);
    }

    /**
     * Get all events
     *
     * @return A collection of all current events
     */
    @RequestMapping(method = RequestMethod.GET)
    public Collection<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    /**
     * Register a team for an event. Only for captains and admin
     *
     * @param teamId  Id of the Team to be registered
     * @param eventId Id of the Event to be registered for.
     *
     * @return Message containing the result of the registration
     */
    @RequestMapping(value = "/{eventId}", method = RequestMethod.POST)
    @PreAuthorize("@currentUserServiceImpl.canEditTeam(principal, #teamId)")
    public ResponseEntity<?> registerTeamForEvent(@RequestBody Long teamId, @PathVariable Long eventId) {
        Team team = teamService.getTeamById(teamId);
        eventService.registerTeam(eventId, team);

        return createResponseEntity(HttpStatus.OK, "Successfully registered Team " + team.getTeamName());
    }

    /**
     * Remove the registration of a team from an Event.
     *
     * @param teamId  Id of the Team to be removed from the Event
     * @param eventId Id of the Event the Team should be removed from
     *
     * @return Statusmessage about the result of the removal
     */
    @RequestMapping(value = "/{eventId}/teams", method = RequestMethod.DELETE)
    @PreAuthorize("@currentUserServiceImpl.canEditTeam(principal, #teamId)")
    public ResponseEntity<?> removeTeamFromEvent(@RequestBody Long teamId, @PathVariable Long eventId) {
        Team team = teamService.getTeamById(teamId);
        eventService.registerTeam(eventId, team);

        return createResponseEntity(HttpStatus.OK, "Successfully registered Team " + team.getTeamName());
    }

    /**
     * Get an overview of all Teams who already registered for the event with the given Id.
     *
     * @param eventId Id of the Event for which to show the registered Teams
     *
     * @return A collection of Teams registered for the Event
     */
    @JsonView(View.Public.class)
    @RequestMapping(value = "/{eventId}/teams", method = RequestMethod.GET)
    public Collection<Team> getRegisteredTeamsForEvent(@PathVariable Long eventId) {
        return eventService.getTeamsForEvent(eventId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return createResponseEntity(HttpStatus.CONFLICT, e.getMessage());
    }
}
