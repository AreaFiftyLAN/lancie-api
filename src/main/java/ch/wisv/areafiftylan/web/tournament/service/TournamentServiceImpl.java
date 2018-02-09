package ch.wisv.areafiftylan.web.tournament.service;

import ch.wisv.areafiftylan.exception.TournamentNotFoundException;
import ch.wisv.areafiftylan.web.tournament.model.Tournament;
import ch.wisv.areafiftylan.web.tournament.model.TournamentType;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class TournamentServiceImpl implements TournamentService {

    private TournamentRepository tournamentRepository;

    public TournamentServiceImpl(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public Collection<Tournament> getTournaments() {
        return tournamentRepository.findAll();
    }

    @Override
    public Collection<Tournament> getTournamentsOfType(TournamentType type) {
        return tournamentRepository.findByType(type);
    }

    @Override
    public Tournament addTournament(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    @Override
    public Tournament replaceTournament(Tournament tournament) {
        if (tournamentRepository.findOne(tournament.getId()) == null) {
            throw new TournamentNotFoundException();
        }
        return tournamentRepository.save(tournament);
    }

    @Override
    public void deleteTournament(Long id) {
        tournamentRepository.delete(id);
    }

    @Override
    public void deleteAllTournaments() {
        tournamentRepository.deleteAll();
    }
}
