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
import org.springframework.security.access.prepost.PreAuthorize;
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

    @RequestMapping("/sponsors")
    public Collection<Sponsor> getSponspors() {
        return sponsorService.getAllSponsors();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/committee")
    public ResponseEntity<?> createCommittee(@RequestBody List<CommitteeMember> committeeMembers) {
        webService.setCommittee(committeeMembers);
        return ResponseEntityBuilder.createResponseEntity(HttpStatus.CREATED, "Committee saved successfully.");
    }

    @GetMapping("/committee")
    public Collection<CommitteeMember> getCommittee() {
        return webService.getCommittee();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/committee")
    public ResponseEntity<?> deleteCommittee() {
        webService.deleteCommittee();
        return ResponseEntityBuilder.createResponseEntity(HttpStatus.OK, "Committee deleted successfully.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/committee/{memberID}")
    public ResponseEntity<?> addCommitteeMember(@PathVariable Long memberID, @RequestBody CommitteeMember committeeMember) {
        webService.addCommitteeMember(memberID, committeeMember);
        return ResponseEntityBuilder.createResponseEntity(HttpStatus.ACCEPTED, "Committee member added successfully.");
    }

    @GetMapping("/committee/{memberID}")
    public CommitteeMember getCommitteeMember(@PathVariable Long memberID) {
        return webService.getCommitteeMember(memberID);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/committee/{memberID}")
    public ResponseEntity<?> deleteCommitteeMember(@PathVariable Long memberID) {
        webService.deleteCommitteeMember(memberID);
        return ResponseEntityBuilder.createResponseEntity(HttpStatus.OK, "Committee member deleted successfully.");
    }
}
