package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.TeamDTO;
import ch.wisv.areafiftylan.dto.TeamInviteResponse;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.security.TeamInviteToken;

import java.util.Collection;
import java.util.List;

public interface TeamService {
    Team create(String username, String teamname);

    Team save(Team team);

    Team getTeamById(Long id);

    Team getTeamByTeamname(String teamname);

    Collection<Team> getTeamByCaptainId(Long userId);

    Collection<Team> getAllTeams();

    Collection<Team> getTeamsByUsername(String username);

    Team update(Long teamId, TeamDTO input);

    Team delete(Long teamId);

    TeamInviteToken inviteMember(Long teamId, String username);

    void removeInvite(String token);

    List<TeamInviteResponse> findTeamInvitesByUsername(String username);

    List<TeamInviteResponse> findTeamInvitesByTeamId(Long teamId);

    void addMemberByInvite(String token);

    void addMember(Long teamId, String username);

    boolean removeMember(Long teamId, String username);
}
