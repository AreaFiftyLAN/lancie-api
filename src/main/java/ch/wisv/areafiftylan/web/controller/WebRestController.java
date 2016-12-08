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


    /**
     * Create a new CommitteeMember. Only available as admin.
     *
     * @param committeeMember The CommitteeMember to add.
     * @return The status of the addition.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/committee")
    public ResponseEntity<?> addCommitteeMember(@RequestBody CommitteeMember committeeMember) {
        webService.addCommitteeMember(committeeMember);
        return ResponseEntityBuilder.createResponseEntity(HttpStatus.CREATED, "Committee member added successfully.");
    }

    /**
     * Retrieve all CommitteeMembers.
     *
     * @return All CommitteeMembers.
     */
    @GetMapping("/committee")
    public ResponseEntity<?> readCommittee() {
        Collection<CommitteeMember> committeeMembers = webService.getCommittee();
        return ResponseEntityBuilder.createResponseEntity(HttpStatus.OK, "Committee members retrieved successfully.", committeeMembers);
    }

    /**
     * Update the CommitteeMember at the given id with the given values.
     * Only available as admin.
     *
     * @param id The id at which to update the CommitteeMember.
     * @param committeeMember The new Values for the CommitteeMember.
     * @return The status of the update.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/committee/{memberID}")
    public ResponseEntity<?> addCommitteeMember(@PathVariable Long id, @RequestBody CommitteeMember committeeMember) {
        webService.updateCommitteeMember(id, committeeMember);
        return ResponseEntityBuilder.createResponseEntity(HttpStatus.ACCEPTED, "Committee member updated successfully.");
    }

    /**
     * Delete a CommitteeMember. Only available as admin.
     *
     * @param id the id of the CommitteeMember to be deleted.
     * @return The status of the deletion.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/committee/{memberID}")
    public ResponseEntity<?> deleteCommitteeMember(@PathVariable Long id) {
        webService.deleteCommitteeMember(id);
        return ResponseEntityBuilder.createResponseEntity(HttpStatus.OK, "Committee member deleted successfully.");
    }
}
