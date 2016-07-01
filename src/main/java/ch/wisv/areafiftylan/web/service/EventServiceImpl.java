/*
 * Copyright (c) 2016  W.I.S.V. 'Christiaan Huygens'
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
