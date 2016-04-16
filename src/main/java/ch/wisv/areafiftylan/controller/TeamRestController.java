package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.TeamDTO;
import ch.wisv.areafiftylan.dto.TeamInviteResponse;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Role;
import ch.wisv.areafiftylan.model.view.View;
import ch.wisv.areafiftylan.security.token.TeamInviteToken;
import ch.wisv.areafiftylan.service.TeamService;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("/teams")
public class TeamRestController {

    private TeamService teamService;

    @Autowired
    public TeamRestController(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * The method to handle POST requests on the /teams endpoint. This creates a new team. Users can only create new
     * Teams with themselves as Captain. Admins can also create Teams with other Users as Captain.
     *
     * @param teamDTO Object containing the Team name and Captain username. When coming from a user, username should
     *                equal their own username.
     * @param auth    Authentication object from Spring Security
     *
     * @return Return status message of the operation
     */
    @PreAuthorize("isAuthenticated() and @currentUserServiceImpl.hasAnyTicket(principal)")
    @JsonView(View.Public.class)
    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(@Validated @RequestBody TeamDTO teamDTO, Authentication auth) {
        Team team;
        // Users can only create teams with themselves as Captain
        if (auth.getAuthorities().contains(Role.ROLE_ADMIN)) {
            team = teamService.create(teamDTO.getCaptainUsername(), teamDTO.getTeamName());
        } else {
            // If the DTO contains another username as the the current user, return an error.
            if (!auth.getName().equals(teamDTO.getCaptainUsername())) {
                return createResponseEntity(HttpStatus.BAD_REQUEST, "Can not create team with another user as Captain");
            }
            team = teamService.create(auth.getName(), teamDTO.getTeamName());
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(team.getId()).toUri());

        return createResponseEntity(HttpStatus.CREATED, httpHeaders,
                "Team successfully created at " + httpHeaders.getLocation(), team);
    }


    /**
     * Get all users. Only available as Admin
     *
     * @return A collection of all registered Users.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(method = RequestMethod.GET)
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
    @JsonView(View.Public.class)
    @RequestMapping(method = RequestMethod.GET, value = "/{teamId}")
    public Team getTeamById(@PathVariable Long teamId) {
        return this.teamService.getTeamById(teamId);
    }

    /**
     * Get the members of the Team with the given Id.
     *
     * @param teamId Id of the Team
     *
     * @return A list of the members of the Team with the given Id. Public information only.
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessTeam(principal, #teamId)")
    @JsonView(View.Public.class)
    @RequestMapping(method = RequestMethod.GET, value = "/{teamId}/members")
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
    @RequestMapping(method = RequestMethod.GET, value = "/{teamId}/members", params = "admin")
    public Set<User> getTeamMembersByIdAdmin(@PathVariable Long teamId) {
        Team team = teamService.getTeamById(teamId);
        return team.getMembers();
    }

    /**
     * Add members to the Team with the given Id. Expects only a username as Requestbody. People can only directly be
     * added to a team by an Admin. Captains can only invite users.
     *
     * @param teamId   Id of the Team to which the User should be added.
     * @param username Username of the User to be added to the Team
     *
     * @return Result message of the request
     */
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(method = RequestMethod.POST, value = "/{teamId}")
    public ResponseEntity<?> addTeamMember(@PathVariable Long teamId, @RequestBody String username) {
        teamService.addMember(teamId, username);
        return createResponseEntity(HttpStatus.OK, "User " + username + " successfully added to Team " + teamId);
    }

    /**
     * Add members to the Team with the given Id. Expects only a username as Requestbody. Can only be done by the
     * Captain or an Admin
     *
     * @param teamId   Id of the Team to which the User should be added.
     * @param username Username of the User to be added to the Team
     *
     * @return Result message of the request
     */
    @PreAuthorize("@currentUserServiceImpl.canEditTeam(principal, #teamId) " +
            "and @currentUserServiceImpl.hasAnyTicket(#username)")
    @RequestMapping(method = RequestMethod.POST, value = "/{teamId}/invites")
    public ResponseEntity<?> inviteTeamMember(@PathVariable Long teamId, @RequestBody String username) {
        TeamInviteToken teamInviteToken = teamService.inviteMember(teamId, username);
        return createResponseEntity(HttpStatus.OK, "User " + username + " successfully invited to Team " + teamId,
                teamInviteToken);
    }

    @PreAuthorize("@currentUserServiceImpl.canEditTeam(principal, #teamId)")
    @RequestMapping(method = RequestMethod.GET, value = "/{teamId}/invites")
    public List<TeamInviteResponse> getTeamInvitesByTeam(@PathVariable Long teamId) {
        return teamService.findTeamInvitesByTeamId(teamId);
    }

    @PreAuthorize("@currentUserServiceImpl.canAcceptInvite(principal, #token)")
    @RequestMapping(method = RequestMethod.POST, value = "/invites")
    public ResponseEntity<?> acceptTeamInvite(@RequestBody String token) {
        teamService.addMemberByInvite(token);
        return createResponseEntity(HttpStatus.OK, "Invite successfully accepted");
    }

    @PreAuthorize("@currentUserServiceImpl.canRevokeInvite(principal, #token)")
    @RequestMapping(method = RequestMethod.DELETE, value = "/invites")
    public ResponseEntity<?> declineTeamInvite(@RequestBody String token) {
        teamService.removeInvite(token);
        return createResponseEntity(HttpStatus.OK, "Invite successfully declined");
    }

    /**
     * Update the Team with the given Id. This can be a rename or a change of Captain. Can only be done by the current
     * Captain or an Admin.
     *
     * @param teamId Id of the Team to be changed
     * @param input  Object containing the team name and Captain username
     *
     * @return The updated Team
     */
    @PreAuthorize("@currentUserServiceImpl.canEditTeam(principal, #teamId)")
    @JsonView(View.Public.class)
    @RequestMapping(method = RequestMethod.PUT, value = "/{teamId}")
    public Team update(@PathVariable Long teamId, @Validated @RequestBody TeamDTO input) {
        return this.teamService.update(teamId, input);
    }

    /**
     * Delete a member from a team. Expects only a username in the RequestBody. Can only be done by the Captain or an
     * Admin
     *
     * @param teamId   Id of the Team to be edited
     * @param username Username of the member to be deleted
     *
     * @return A status message of the operation
     */
    @PreAuthorize("@currentUserServiceImpl.canRemoveFromTeam(principal, #teamId, #username)")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{teamId}/members")
    public ResponseEntity<?> removeTeamMember(@PathVariable Long teamId, @RequestBody String username) {
        teamService.removeMember(teamId, username);
        return createResponseEntity(HttpStatus.OK, "User " + username + " successfully removed from Team " + teamId);
    }

    /**
     * Delete an entire Team. Limit this for Admins for now
     *
     * @param teamId Id of the Team to be deleted
     *
     * @return The deleted Team
     */
    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(View.Public.class)
    @RequestMapping(method = RequestMethod.DELETE, value = "{teamId}")
    public ResponseEntity<?> delete(@PathVariable Long teamId) {

        Team deletedTeam = teamService.delete(teamId);
        return createResponseEntity(HttpStatus.OK, "Deleted team with " + teamId, deletedTeam);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return createResponseEntity(HttpStatus.CONFLICT, e.getMessage());
    }
}