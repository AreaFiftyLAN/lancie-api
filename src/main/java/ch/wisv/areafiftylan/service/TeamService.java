package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.Team;

import java.util.Collection;

public interface TeamService {
    Team getTeamById(Long id);

    Team getTeamByTeamname(String teamname);

    Team getTeamByCaptainId(Long userId);

    Collection<Team> getAllTeams();

    Collection<Team> getTeamsByUsername(String username);

}
