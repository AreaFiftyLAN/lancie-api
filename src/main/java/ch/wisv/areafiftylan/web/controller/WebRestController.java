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

import ch.wisv.areafiftylan.utils.ResponseEntityBuilder;
import ch.wisv.areafiftylan.web.model.CommitteeMember;
import ch.wisv.areafiftylan.web.model.Event;
import ch.wisv.areafiftylan.web.model.Sponsor;
import ch.wisv.areafiftylan.web.model.Tournament;
import ch.wisv.areafiftylan.web.service.EventServiceImpl;
import ch.wisv.areafiftylan.web.service.SponsorServiceImpl;
import ch.wisv.areafiftylan.web.service.TournamentServiceImpl;
import ch.wisv.areafiftylan.web.service.WebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/web")
public class WebRestController {

    private final TournamentServiceImpl tournamentService;
    private final EventServiceImpl eventService;
    private final SponsorServiceImpl sponsorService;

    private WebService webService;

    @Autowired
    public WebRestController(TournamentServiceImpl tournamentService, EventServiceImpl eventService,
                             SponsorServiceImpl sponsorService, WebService webService) {
        this.tournamentService = tournamentService;
        this.eventService = eventService;
        this.sponsorService = sponsorService;
        this.webService = webService;
    }

    @RequestMapping("/tournaments")
    public Collection<Tournament> getAllTournaments() {
        return tournamentService.getAllTournaments();
    }

    @RequestMapping("/events")
    public Collection<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    /**
     * Returns all committee members. This is done in the Controller because it's simple and static nature
     *
     * @return A collection with all committee members.
     */
    @GetMapping("/committee")
    public List<CommitteeMember> getCommittee() {
        return webService.getAllCommitteeMembers();
    }

    @PutMapping("/committee")
    public ResponseEntity<?> setCommittee(@RequestBody List<CommitteeMember> committeeMembers) {
        webService.setAllCommitteeMembers(committeeMembers);
        return ResponseEntityBuilder.createResponseEntity(HttpStatus.OK, "Committee members saved succesfully.");
    }

    @PostMapping("/committee")
    public ResponseEntity<?> setCommitteeMember(@RequestBody CommitteeMember committeeMember) {
        webService.addCommitteeMember(committeeMember);
        return ResponseEntityBuilder.createResponseEntity(HttpStatus.OK, "Committee member added succesfully.");
    }

    @RequestMapping("/sponsors")
    public Collection<Sponsor> getSponspors() {
        return sponsorService.getAllSponsors();
    }
}
