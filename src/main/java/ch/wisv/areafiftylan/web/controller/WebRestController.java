package ch.wisv.areafiftylan.web.controller;

import ch.wisv.areafiftylan.web.model.Tournament;
import ch.wisv.areafiftylan.web.service.TournamentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/web")
public class WebRestController {

    TournamentServiceImpl tournamentServiceImpl;

    @Autowired
    public WebRestController(TournamentServiceImpl tournamentServiceImpl) {
        this.tournamentServiceImpl = tournamentServiceImpl;
    }

    @RequestMapping("/tournaments")
    public Collection<Tournament> getAllTournamentsFromJsonFile() {
        return tournamentServiceImpl.getAllTournaments();
    }
}
