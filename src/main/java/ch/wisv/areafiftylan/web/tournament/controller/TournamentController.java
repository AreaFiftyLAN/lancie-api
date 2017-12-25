package ch.wisv.areafiftylan.web.tournament.controller;

import ch.wisv.areafiftylan.exception.TournamentNotFoundException;
import ch.wisv.areafiftylan.web.tournament.model.Tournament;
import ch.wisv.areafiftylan.web.tournament.model.TournamentType;
import ch.wisv.areafiftylan.web.tournament.service.TournamentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@Controller
@RequestMapping("/web/tournament")
public class TournamentController {

    private TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping
    ResponseEntity<?> getTournaments() {
        Collection<Tournament> tournaments = tournamentService.getTournaments();
        return createResponseEntity(HttpStatus.OK, "Tournaments retrieved.", tournaments);
    }

    @GetMapping("/{type}")
    ResponseEntity<?> getTournamentsOfType(@PathVariable TournamentType type) {
        Collection<Tournament> tournamentsOfType = tournamentService.getTournamentsOfType(type);
        return createResponseEntity(HttpStatus.OK, "Tournaments of type " + type + " retrieved.", tournamentsOfType);
    }

    @PostMapping
    @PreAuthorize("hasRole('COMMITTEE')")
    ResponseEntity<?> addTournament(@RequestBody Tournament tournament) {
        tournament = tournamentService.addTournament(tournament);
        return createResponseEntity(HttpStatus.CREATED, "Tournament added.", tournament);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COMMITTEE')")
    ResponseEntity<?> editTournament(@RequestBody Tournament tournament) {
        final Tournament finalTournament = tournament;
        tournamentService.getTournaments()
                .stream()
                .filter(dbTournament -> dbTournament.getId().equals(finalTournament.getId()))
                .findFirst()
                .orElseThrow(TournamentNotFoundException::new);
        tournament = tournamentService.addTournament(tournament);
        return createResponseEntity(HttpStatus.CREATED, "Tournament updated.", tournament);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COMMITTEE')")
    ResponseEntity<?> deleteTournament(@PathVariable Long id) {
        tournamentService.deleteTournament(id);
        return createResponseEntity(HttpStatus.OK, "Tournament deleted.");
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<?> deleteAllTournaments() {
        tournamentService.deleteAllTournaments();
        return createResponseEntity(HttpStatus.OK, "All Tournaments deleted.");
    }
}
