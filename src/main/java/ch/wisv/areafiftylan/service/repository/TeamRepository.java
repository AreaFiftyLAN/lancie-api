package ch.wisv.areafiftylan.service.repository;

import ch.wisv.areafiftylan.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
//    Collection<Team> findByMembersUsername(String username);

    Optional<Team> findByCaptainId(Long userId);

    Team findByTeamName(String teamName);

}
