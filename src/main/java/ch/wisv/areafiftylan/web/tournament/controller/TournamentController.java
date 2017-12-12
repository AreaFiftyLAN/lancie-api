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

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('COMMITTEE')")
    ResponseEntity<?> editTournament(@RequestBody Tournament tournament) {
        Tournament dbTournament = tournamentService.getTournaments()
                .stream()
                .filter(tournament1 -> tournament1.getId().equals(tournament.getId()))
                .findFirst()
                .orElseThrow(TournamentNotFoundException::new);

        // This is a bit double, but it ensures we update an existing tournament
        dbTournament.setButtonImagePath(tournament.getButtonImagePath());
        dbTournament.setButtonTitle(tournament.getButtonTitle());
        dbTournament.setDescription(tournament.getDescription());
        dbTournament.setFormat(tournament.getFormat());
        dbTournament.setHeaderTitle(tournament.getHeaderTitle());
        dbTournament.setPrizes(tournament.getPrizes());
        dbTournament.setSponsor(tournament.getSponsor());
        dbTournament.setType(tournament.getType());

        dbTournament = tournamentService.addTournament(dbTournament);
        return createResponseEntity(HttpStatus.CREATED, "Tournament updated.", dbTournament);
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
