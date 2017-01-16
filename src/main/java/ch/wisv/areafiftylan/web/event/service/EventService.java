package ch.wisv.areafiftylan.web.event.service;

import ch.wisv.areafiftylan.web.event.model.Event;

import java.util.Collection;

public interface EventService {

    void addEvent(Event event);

    Collection<Event> getEvents();

    void updateEvent(Long id, Event event);

    void deleteEvent(Long id);
}
