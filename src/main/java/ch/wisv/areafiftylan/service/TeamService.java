package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.TeamDTO;
import ch.wisv.areafiftylan.model.Team;

import javax.swing.text.html.Option;
import java.util.Collection;
import java.util.Optional;

public interface TeamService {
    Team create(TeamDTO team);

    Team save(Team team);

    Optional<Team> getTeamById(Long id);

    Optional<Team> getTeamByTeamname(String teamname);

    Team getTeamByCaptainId(Long userId);

    Collection<Team> getAllTeams();

    Collection<Team> getTeamsByUsername(String username);

    Team update(Long teamId, TeamDTO input);
}
