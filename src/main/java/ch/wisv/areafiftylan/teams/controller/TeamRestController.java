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

package ch.wisv.areafiftylan.teams.controller;

import ch.wisv.areafiftylan.seats.model.Seat;
import ch.wisv.areafiftylan.seats.service.SeatService;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.teams.model.TeamDTO;
import ch.wisv.areafiftylan.teams.model.TeamInviteResponse;
import ch.wisv.areafiftylan.teams.service.TeamService;
import ch.wisv.areafiftylan.users.model.Role;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.utils.view.View;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("/teams")
public class TeamRestController {

    private final TeamService teamService;

    private final SeatService seatService;

    @Autowired
    public TeamRestController(TeamService teamService, SeatService seatService) {
        this.teamService = teamService;
        this.seatService = seatService;
    }

    /**
     * The method to handle POST requests on the /teams endpoint. This creates a new team. Users can only create new
     * Teams with themselves as Captain. Admins can also create Teams with other Users as Captain.
     *
     * @param teamDTO Object containing the Team name and Captain email. When coming from a user, email should
     *                equal their own email.
     * @param user    The logged in user
     *
     * @return Return status message of the operation
     */
    @PreAuthorize("isAuthenticated()")
    @JsonView(View.Public.class)
    @PostMapping
    ResponseEntity<?> add(@AuthenticationPrincipal User user, @Validated @RequestBody TeamDTO teamDTO) {
        if (teamService.teamnameUsed(teamDTO.getTeamName())) {
            return createResponseEntity(HttpStatus.CONFLICT,
                    "Team with name \"" + teamDTO.getTeamName() + "\" already exists.");
        }

        Team team;
        // Users can only create teams with themselves as Captain
        if (user.getAuthorities().contains(Role.ROLE_ADMIN)) {
            team = teamService.create(teamDTO.getCaptainEmail(), teamDTO.getTeamName());
        } else {
            // If the DTO contains another email as the the current user, return an error.
            if (!user.getEmail().equalsIgnoreCase(teamDTO.getCaptainEmail())) {
                return createResponseEntity(HttpStatus.BAD_REQUEST, "Can not create team with another user as Captain");
            }
            team = teamService.create(user.getEmail(), teamDTO.getTeamName());
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(team.getId()).toUri());

        return createResponseEntity(HttpStatus.CREATED, httpHeaders,
                "Team successfully created at " + httpHeaders.getLocation(), team);
    }

    /**
     * The method to handle POST requests on the /teams endpoint. This assigns a new captain to an existing team.
     * Users can only assign a new captain to a team if they are the captain of that team.
     * Admins can also assign a new captain to a team.
     *
     * @param user The logged in user
     * @param teamID The ID of the team
     * @param newCaptainEmail The email of the new captain
     *
     * @return Return status message of the operation
     */
    @PreAuthorize("@currentUserServiceImpl.canEditTeam(principal, #teamID)")
    @JsonView(View.Public.class)
    @PostMapping("/{teamID}/captain")
    ResponseEntity<?> changeCaptain(@AuthenticationPrincipal User user, @PathVariable Long teamID,
                                    @RequestBody String newCaptainEmail) {
        Team team = teamService.getTeamById(teamID);

        if (team.getCaptain().getEmail().equals(newCaptainEmail)) {
            return createResponseEntity(HttpStatus.NOT_MODIFIED, "This person is already the captain of the team.");
        }
        
        teamService.update(team.getId(), null, newCaptainEmail);
        return createResponseEntity(HttpStatus.OK,
                "Captain of team " + team.getTeamName() + " successfully changed!");
    }


    /**
     * Get all users. Only available as Admin
     *
     * @return A collection of all registered Users.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Collection<Team> readTeams() {
        return teamService.getAllTeams();
    }

    /**
     * Get the team based on its Id. Can only be accessed by Team members or Admins.
     *
     * @param teamId Id of the team
     *
     * @return A representation the Team with given Id.
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessTeam(principal, #teamId)")
    @JsonView(View.Team.class)
    @GetMapping("/{teamId}")
    public Team getTeamById(@PathVariable Long teamId) {
        return this.teamService.getTeamById(teamId);
    }

    /**
     * Get the Seats of a Team with the given teamname
     *
     * @param teamName Name of the Team you want the Seats of.
     *
     * @return Collection of Seats belonging to members of the Team.
     */
    @JsonView(View.Public.class)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{teamName}/seats")
    public Collection<Seat> getSeatsForTeam(@PathVariable String teamName) {
        return seatService.getSeatsByTeamName(teamName);
    }
    
    /**
     * Get the members of the Team with the given Id.
     *
     * @param teamId Id of the Team
     *
     * @return A list of the members of the Team with the given Id. Public information only.
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessTeam(principal, #teamId)")
    @JsonView(View.Team.class)
    @GetMapping("/{teamId}/members")
    public Set<User> getTeamMembersById(@PathVariable Long teamId) {
        Team team = teamService.getTeamById(teamId);
        return team.getMembers();
    }

    /**
     * This is the admin version of the above method. Instead of showing only the public fields, this method returns the
     * full users with profiles. This method can be reached with the "/teams/{teamId}/members?admin" url. Of course,
     * only accessible by admins.
     *
     * @param teamId Id of the Team
     *
     * @return A list of the members of the Team with the given Id
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/{teamId}/members", params = "admin")
    public Set<User> getTeamMembersByIdAdmin(@PathVariable Long teamId) {
        Team team = teamService.getTeamById(teamId);
        return team.getMembers();
    }

    /**
     * Add members to the Team with the given Id. Expects only an email as Requestbody. People can only directly be
     * added to a team by an Admin. Captains can only invite users.
     *
     * @param teamId   Id of the Team to which the User should be added.
     * @param email Email of the User to be added to the Team
     *
     * @return Result message of the request
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{teamId}")
    public ResponseEntity<?> addTeamMember(@PathVariable Long teamId, @RequestBody String email) {
        teamService.addMember(teamId, email);
        return createResponseEntity(HttpStatus.OK, "User " + email + " successfully added to Team " + teamId);
    }

    /**
     * Add members to the Team with the given Id. Expects only an email as Requestbody. Can only be done by the
     * Captain or an Admin
     *
     * @param teamId   Id of the Team to which the User should be added.
     * @param email Email of the User to be added to the Team
     *
     * @return Result message of the request
     */
    @PreAuthorize("@currentUserServiceImpl.canEditTeam(principal, #teamId) ")
    @PostMapping("/{teamId}/invites")
    public ResponseEntity<?> inviteTeamMember(@PathVariable Long teamId, @RequestBody String email) {
        teamService.inviteMember(teamId, email);
        return createResponseEntity(HttpStatus.OK, "User " + email + " successfully invited to Team " + teamId);
    }

    @PreAuthorize("@currentUserServiceImpl.canEditTeam(principal, #teamId)")
    @GetMapping("/{teamId}/invites")
    public List<TeamInviteResponse> getTeamInvitesByTeam(@PathVariable Long teamId) {
        return teamService.findTeamInvitesByTeamId(teamId);
    }

    /**
     * Accept an invite
     *
     * @param token Token of the Invite
     *
     * @return Statusmessage
     */
    @PreAuthorize("@currentUserServiceImpl.canAcceptInvite(principal, #token)")
    @PostMapping("/invites")
    public ResponseEntity<?> acceptTeamInvite(@RequestBody String token) {
        teamService.addMemberByInvite(token);
        return createResponseEntity(HttpStatus.OK, "Invite successfully accepted");
    }

    /**
     * Decline or revoke an invite
     *
     * @param token Token of the Invite
     *
     * @return Statusmessage
     */
    @PreAuthorize("@currentUserServiceImpl.canRevokeInvite(principal, #token)")
    @DeleteMapping("/invites")
    public ResponseEntity<?> revokeTeamInvite(@RequestBody String token) {
        teamService.revokeInvite(token);
        return createResponseEntity(HttpStatus.OK, "Invite successfully declined");
    }

    /**
     * Update the Team with the given Id. This can be a rename or a change of Captain. Can only be done by the current
     * Captain or an Admin.
     *
     * @param teamId Id of the Team to be changed
     * @param input  Object containing the team name and Captain email
     *
     * @return The updated Team
     */
    @PreAuthorize("@currentUserServiceImpl.canEditTeam(principal, #teamId)")
    @JsonView(View.Team.class)
    @PutMapping("/{teamId}")
    public Team update(@PathVariable Long teamId, @Validated @RequestBody TeamDTO input) {
        return this.teamService.update(teamId, input.getTeamName(), input.getCaptainEmail());
    }

    /**
     * Delete a member from a team. Expects only an email in the RequestBody. The team will be deleted when the capain
     * is the last member.
     *
     * @param teamId   Id of the Team to be edited
     * @param email Email of the member to be deleted
     *
     * @return A status message of the operation
     */
    @PreAuthorize("@currentUserServiceImpl.canRemoveFromTeam(principal, #teamId, #email)")
    @DeleteMapping("/{teamId}/members")
    public ResponseEntity<?> removeTeamMember(@PathVariable Long teamId, @RequestBody String email) {
        if (teamService.removeMember(teamId, email)) {
            return createResponseEntity(HttpStatus.OK, "User '" + email + "' successfully removed from Team " + teamId);
        } else {
            return createResponseEntity(HttpStatus.FORBIDDEN, "Cannot remove captain from team with other members.");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return createResponseEntity(HttpStatus.CONFLICT, e.getMessage());
    }
}
