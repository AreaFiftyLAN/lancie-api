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

package ch.wisv.areafiftylan.web.controller;

import ch.wisv.areafiftylan.web.model.CommitteeMember;
import ch.wisv.areafiftylan.web.model.Event;
import ch.wisv.areafiftylan.web.model.Sponsor;
import ch.wisv.areafiftylan.web.model.Tournament;
import ch.wisv.areafiftylan.web.service.EventServiceImpl;
import ch.wisv.areafiftylan.web.service.SponsorServiceImpl;
import ch.wisv.areafiftylan.web.service.TournamentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;

@RestController
@RequestMapping("/web")
public class WebRestController {

    private final TournamentServiceImpl tournamentService;
    private final EventServiceImpl eventService;
    private final SponsorServiceImpl sponsorService;

    @Autowired
    public WebRestController(TournamentServiceImpl tournamentService, EventServiceImpl eventService,
                             SponsorServiceImpl sponsorService) {
        this.tournamentService = tournamentService;
        this.eventService = eventService;
        this.sponsorService = sponsorService;
    }

    @GetMapping("/tournaments")
    public Collection<Tournament> getAllTournaments() {
        return tournamentService.getAllTournaments();
    }

    @GetMapping("/events")
    public Collection<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    /**
     * Returns all committee members. This is done in the Controller because it's simple and static nature
     *
     * @return A collection with all committee members.
     */
    @GetMapping("/committee")
    public Collection<CommitteeMember> getCommittee() {
        Collection<CommitteeMember> committeeMembers = new ArrayList<>();
        committeeMembers.add(new CommitteeMember("Sille Kamoen", "Chairman", "people"));
        committeeMembers.add(new CommitteeMember("Rebecca Glans", "Secretary", "women"));
        committeeMembers.add(new CommitteeMember("Sven Popping", "Treasurer", "money"));

        return committeeMembers;
    }

    @GetMapping("/sponsors")
    public Collection<Sponsor> getSponspors() {
        return sponsorService.getAllSponsors();
    }
}
