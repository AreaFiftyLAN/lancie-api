package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.EventDTO;
import ch.wisv.areafiftylan.exception.EventException;
import ch.wisv.areafiftylan.model.Event;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.service.repository.EventRepository;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

/**
 * Created by Sille Kamoen on 27-3-16.
 */
@Service
public class EventServiceImpl implements EventService {

    private EventRepository eventRepository;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void registerTeam(Long eventId, Team team) {
        Event event = getEventById(eventId);
        if (event.getTeamSize() != team.getSize()) {
            throw new EventException("Message is not of the right size!");
        }

        if (event.getTeamLimit() == event.getRegisteredTeams().size()) {
            throw new EventException("Event is full!");
        }

        if (!event.isOpenForRegistration()) {
            throw new EventException("Event is not open for registration!");
        }

        // Check if one of the Team members is already a member
        boolean duplicateUser = event.getRegisteredTeams().stream().flatMap(t -> t.getMembers().stream())
                .anyMatch(u -> team.getMembers().contains(u));

        if(duplicateUser){
            throw new EventException("A Member on your Team is already registered");
        }

        // We're registering the Team here
        if (!event.addTeam(team)) {
            throw new EventException("Something went wrong while registrating");
        }

        eventRepository.save(event);
    }

    @Override
    public void removeTeamFromEvent(Long eventId, Team team) {
        Event event = getEventById(eventId);
        event.getRegisteredTeams().remove(team);
        eventRepository.save(event);
    }

    @Override
    public Event addEvent(EventDTO eventDTO) {
        eventRepository.findByEventName(eventDTO.getName()).ifPresent(e -> {
            throw new IllegalArgumentException("eventname " + e.getEventName() + " already exists");
        });
        Event event = new Event(eventDTO.getName(), eventDTO.getTeamSize(), eventDTO.getTeamLimit());
        return eventRepository.saveAndFlush(event);
    }

    @Override
    public Event updateEvent(Long eventId, EventDTO eventDTO) {
        Event event = getEventById(eventId);

        if (!Strings.isNullOrEmpty(eventDTO.getName())) {
            event.setEventName(eventDTO.getName());
        }

        if (eventDTO.getTeamLimit() != null) {
            event.setTeamLimit(eventDTO.getTeamLimit());
        }

        if (eventDTO.getTeamSize() != null) {
            event.setTeamSize(eventDTO.getTeamSize());
        }

        return eventRepository.save(event);
    }

    @Override
    public Set<Team> getTeamsForEvent(Long eventId) {
        Event event = getEventById(eventId);
        return event.getRegisteredTeams();
    }

    @Override
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new EventException(eventId));
    }

    @Override
    public Event deleteEvent(Long eventId) {
        Event event = getEventById(eventId);
        eventRepository.delete(eventId);
        return event;
    }

    @Override
    public void setOpenForRegistration(Long eventId, boolean openForRegistration) {
        Event event = getEventById(eventId);
        event.setOpenForRegistration(openForRegistration);
    }

    @Override
    public Collection<Event> getAllEvents() {
        return eventRepository.findAll();
    }
}
