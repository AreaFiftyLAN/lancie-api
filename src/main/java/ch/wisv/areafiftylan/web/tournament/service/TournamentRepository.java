package ch.wisv.areafiftylan.web.tournament.service;

import ch.wisv.areafiftylan.web.tournament.model.Tournament;
import ch.wisv.areafiftylan.web.tournament.model.TournamentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    Collection<Tournament> findByType(TournamentType type);

}
