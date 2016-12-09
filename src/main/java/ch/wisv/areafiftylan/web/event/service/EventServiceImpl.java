package ch.wisv.areafiftylan.web.event.service;

import ch.wisv.areafiftylan.web.event.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class EventServiceImpl implements EventService {

    private EventRepository eventRepository;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void addEvent(Event event) {
        eventRepository.saveAndFlush(event);
    }

    @Override
    public Collection<Event> getEvents() {
        return eventRepository.findAll();
    }

    @Override
    public void updateEvent(Long id, Event event) {
        Event oldEvent = eventRepository.getOne(id);
        oldEvent.setName(event.getName());
        oldEvent.setStartTime(event.getStartTime());
        oldEvent.setEndTime(event.getEndTime());
    }

    @Override
    public void deleteEvent(Long id) {
        eventRepository.delete(id);
    }
}
