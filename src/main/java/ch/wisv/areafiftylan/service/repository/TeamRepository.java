package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Collection;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Collection<Team> findAllByMembersUsername(String username);

    Collection<Team> findByCaptainId(Long userId);

    Optional<Team> findById(Long teamId);

    Collection<Team> findAllByCaptainUsername(String username);

    Optional<Team> findByTeamName(String teamName);
}
