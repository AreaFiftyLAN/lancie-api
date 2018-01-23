package ch.wisv.areafiftylan.web.tournament.service;

import ch.wisv.areafiftylan.web.tournament.model.Tournament;
import ch.wisv.areafiftylan.web.tournament.model.TournamentType;

import java.util.Collection;

public interface TournamentService {

    Collection<Tournament> getTournaments();

    Collection<Tournament> getTournamentsOfType(TournamentType type);

    Tournament addTournament(Tournament tournament);

    Tournament replaceTournament(Tournament tournament);

    void deleteTournament(Long id);

    void deleteAllTournaments();
}
