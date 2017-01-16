package ch.wisv.areafiftylan.web.event.controller;

import ch.wisv.areafiftylan.utils.ResponseEntityBuilder;
import ch.wisv.areafiftylan.web.event.model.Event;
import ch.wisv.areafiftylan.web.event.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/web/events")
public class EventRestController {

    private EventService eventService;

    @Autowired
    public EventRestController(EventService eventService) {
        this.eventService = eventService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> addEvent(@RequestBody Event event) {
        eventService.addEvent(event);
        return ResponseEntityBuilder.createResponseEntity(HttpStatus.CREATED, "Event added successfully.");
    }

    @GetMapping
    public ResponseEntity<?> readEvents() {
        Collection<Event> events = eventService.getEvents();
        return ResponseEntityBuilder.createResponseEntity(HttpStatus.OK, "Events retrieved successfully.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{eventID}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody Event event) {
        eventService.updateEvent(id, event);
        return ResponseEntityBuilder.createResponseEntity(HttpStatus.ACCEPTED, "Event updated successfully.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{eventID}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntityBuilder.createResponseEntity(HttpStatus.OK, "Event deleted successfully.");
    }

}
