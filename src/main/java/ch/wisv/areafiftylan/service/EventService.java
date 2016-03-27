package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.Event;
import ch.wisv.areafiftylan.model.EventDTO;
import ch.wisv.areafiftylan.model.Team;

import java.util.Collection;
import java.util.Set;

/**
 * Created by Sille Kamoen on 27-3-16.
 */
public interface EventService {

    /**
     * Register a Team for the Event with the given Id.
     *
     * @param eventId Id of the Event to be registered for
     * @param team    Team that should be registered for the Event
     */
    public void registerTeam(Long eventId, Team team);

    /**
     * Remove the registration of the Team for the Event
     *
     * @param eventId Id of the Event the Team should be removed from
     * @param team    The Team that should be removed from the Event
     */
    public void removeTeamFromEvent(Long eventId, Team team);

    /**
     * Add a new Event
     *
     * @param eventDTO DTO containing all information for the Event
     *
     * @return The newly created Event;
     */
    public Event addEvent(EventDTO eventDTO);

    /**
     * Change the Event with the given Id;
     *
     * @param eventId  Id of the Event to be changed.
     * @param eventDTO DTO containing the new info for the Event
     *
     * @return The newly updated Event
     */
    public Event updateEvent(Long eventId, EventDTO eventDTO);

    /**
     * Get all Teams currently registered for the Event
     *
     * @param eventId Id of the Event
     *
     * @return A Set of Teams currently registered for the Event
     */
    public Set<Team> getTeamsForEvent(Long eventId);

    /**
     * Get the Event by its Id;
     *
     * @param eventId Id of the Event.
     *
     * @return Event with the given id;
     */
    public Event getEventById(Long eventId);

    /**
     * Delete the Event with the given Id;
     *
     * @param eventId Id of the Event to be removed
     *
     * @return The removed Event.
     */
    public Event deleteEvent(Long eventId);

    /**
     * Enable the Event for registration or close it.
     *
     * @param eventId             Id of the Event to be locked/unlocked
     * @param openForRegistration Boolean whether the event should be open for registration or not.
     */
    public void setOpenForRegistration(Long eventId, boolean openForRegistration);

    /**
     * Get all Events
     *
     * @return A Collection of all current Events.
     */
    public Collection<Event> getAllEvents();
}
