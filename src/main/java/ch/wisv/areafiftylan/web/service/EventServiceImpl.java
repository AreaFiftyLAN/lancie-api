package ch.wisv.areafiftylan.web.service;

import ch.wisv.areafiftylan.web.model.Event;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class EventServiceImpl {

    public Collection<Event> getAllEvents() {
        return getDummyEvents();
    }

    private Collection<Event> getDummyEvents() {
        Collection<Event> events = new ArrayList<>();
        events.add(new Event("Arcade", "subtitle", "Arcade Games", "Pinball and shit. Awesomesauce",
                "/path/to/background"));
        events.add(new Event("Sport", "Good Morning!", "HIE HA OCHTEND GYMNASTIEK", "The Mountain Pose",
                "/path/to/background"));
        events.add(new Event("Offline", "Board games", "Offline Board games", "Poker, Monopoly and cluedo",
                "/path/to/background"));
        return events;
    }
}
