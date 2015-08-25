package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Collection;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Collection<Team> findAllByMembersUsername(String username);

    Optional<Team> findByCaptainId(Long userId);

    Collection<Team> findAllByCaptainUsername(String username);

    Team findByTeamName(String teamName);
}
