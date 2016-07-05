package ch.wisv.areafiftylan.web.service;

import ch.wisv.areafiftylan.web.model.WebEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class WebEventsService {

    public Collection<WebEvent> getAllEvents() {
        return getDummyEvents();
    }

    private Collection<WebEvent> getDummyEvents() {
        Collection<WebEvent> webEvents = new ArrayList<>();
        webEvents.add(new WebEvent("Arcade", "subtitle", "Arcade Games", "Pinball and shit. Awesomesauce",
                "/path/to/background"));
        webEvents.add(new WebEvent("Sport", "Good Morning!", "HIE HA OCHTEND GYMNASTIEK", "The Mountain Pose",
                "/path/to/background"));
        webEvents.add(new WebEvent("Offline", "Board games", "Offline Board games", "Poker, Monopoly and cluedo",
                "/path/to/background"));
        return webEvents;
    }
}
