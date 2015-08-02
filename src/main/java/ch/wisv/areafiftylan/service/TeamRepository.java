package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
//    Collection<Team> findByMembersUsername(String username);

//    Collection<Team> findByCaptainUsername(String username);

    Team findByTeamName(String teamName);

}
