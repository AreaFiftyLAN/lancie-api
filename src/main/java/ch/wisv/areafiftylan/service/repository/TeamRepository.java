package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Collection<Team> findAllByMembersUsernameIgnoreCase(String username);

    Collection<Team> findByCaptainId(Long userId);

    Collection<Team> findAllByCaptainUsernameIgnoreCase(String username);

    Optional<Team> findByTeamNameIgnoreCase(String teamName);

    Optional<Team> findById(Long teamId);
}
