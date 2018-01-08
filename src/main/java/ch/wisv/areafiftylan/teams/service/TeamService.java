/*
 * Copyright (c) 2018  W.I.S.V. 'Christiaan Huygens'
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.wisv.areafiftylan.teams.service;

import ch.wisv.areafiftylan.security.token.TeamInviteToken;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.teams.model.TeamDTO;
import ch.wisv.areafiftylan.teams.model.TeamInviteResponse;

import java.util.Collection;
import java.util.List;

public interface TeamService {
    Team create(String email, String teamname);

    Team getTeamById(Long id);

    Team getTeamByTeamname(String teamname);

    boolean teamnameUsed(String teamname);

    Collection<Team> getTeamByCaptainId(Long userId);

    Collection<Team> getAllTeams();

    Collection<Team> getTeamsByMemberEmail(String email);

    Team update(Long teamId, TeamDTO input);

    Team delete(Long teamId);

    TeamInviteToken inviteMember(Long teamId, String email);

    void revokeInvite(String token);

    List<TeamInviteResponse> findTeamInvitesByEmail(String email);

    List<TeamInviteResponse> findTeamInvitesByTeamId(Long teamId);

    void addMemberByInvite(String token);

    void addMember(Long teamId, String email);

    boolean removeMember(Long teamId, String email);
}
