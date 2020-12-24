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


package ch.wisv.areafiftylan.integration;

import ch.wisv.areafiftylan.security.token.TeamInviteToken;
import ch.wisv.areafiftylan.security.token.repository.TeamInviteTokenRepository;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.teams.service.TeamRepository;
import ch.wisv.areafiftylan.users.model.User;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;


public class TeamRestIntegrationTest extends XAuthIntegrationTest {

    @Autowired
    protected TeamRepository teamRepository;

    @Autowired
    private TeamInviteTokenRepository teamInviteTokenRepository;

    private final String TEAM_ENDPOINT = "/teams/";

    private Map<String, String> getTeamDTO(User captain) {
        Map<String, String> team = new HashMap<>();
        team.put("captainEmail", captain.getEmail());
        team.put("teamName", "Team + " + captain.getId());
        return team;

    }

    //region Test Create Teams
    @Test
    public void testCreateTeamAsCaptain() {
        User captain = createUser();
        Map<String, String> teamDTO = getTeamDTO(captain);

        //@formatter:off
        Integer teamId =
            given().
                header(getXAuthTokenHeaderForUser(captain)).
            when().
                body(teamDTO).
                contentType(ContentType.JSON).
                post(TEAM_ENDPOINT).
            then().
                statusCode(HttpStatus.SC_CREATED).
                header("Location", containsString("/teams/")).
                body("object.teamName", equalTo(teamDTO.get("teamName"))).
                body("object.captain.profile.displayName", equalTo(captain.getProfile().getDisplayName())).
                body("object.members", hasSize(1)).
            extract().response().path("object.id");
        //@formatter:on

        Team team = teamRepository.getOne(Long.valueOf(teamId));
        assertNotNull(team);
    }

    @Test
    public void testCreateTeamAsCaptainDifferentCase() {
        User captain = createUser();
        Map<String, String> teamDTO = getTeamDTO(captain);
        teamDTO.put("captainEmail", captain.getEmail().toUpperCase());

        //@formatter:off
        Integer teamId =
            given().
                header(getXAuthTokenHeaderForUser(captain)).
            when().
                body(teamDTO).contentType(ContentType.JSON).
                post(TEAM_ENDPOINT).
            then().
                statusCode(HttpStatus.SC_CREATED).
                header("Location", containsString("/teams/")).
                body("object.teamName", equalTo(teamDTO.get("teamName"))).
                body("object.captain.profile.displayName", equalTo(captain.getProfile().getDisplayName())).
                body("object.members", hasSize(1)).
            extract().response().path("object.id");
        //@formatter:on

        Team team = teamRepository.getOne(Long.valueOf(teamId));
        assertNotNull(team);
    }

    @Test
    public void testCreateTeamWithoutTicket() {
        User captain = createUser();
        Map<String, String> teamDTO = getTeamDTO(captain);

        //@formatter:off
        Integer teamId =
            given().
                header(getXAuthTokenHeaderForUser(captain)).
            when().
                body(teamDTO).
                contentType(ContentType.JSON).
                post(TEAM_ENDPOINT).
            then().
                statusCode(HttpStatus.SC_CREATED).
                header("Location", containsString("/teams/")).
                body("object.teamName", equalTo(teamDTO.get("teamName"))).
                body("object.captain.profile.displayName", equalTo(captain.getProfile().getDisplayName())).
                body("object.members", hasSize(1)).
            extract().response().path("object.id");
        //@formatter:on

        Team team = teamRepository.getOne(Long.valueOf(teamId));
        assertNotNull(team);
    }

    @Test
    public void testCreateTeamAsUserMissingCaptainParameter() {

        User captain = createUser();
        Map<String, String> teamDTO = getTeamDTO(captain);
        teamDTO.remove("captainEmail");


        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            body(teamDTO).contentType(ContentType.JSON).
            post(TEAM_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testCreateTeamWithDifferentCaptainEmail() {
        User user = createUser();
        User captain = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            body(getTeamDTO(user)).contentType(ContentType.JSON).
            post(TEAM_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testCreateTeamAsAdminWithDifferentCaptain() {
        User admin = createAdmin();
        User captain = createUser();

        Map<String, String> teamDTO = getTeamDTO(captain);
        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            body(teamDTO).contentType(ContentType.JSON).
            post(TEAM_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_CREATED).
            header("Location", containsString("/teams/")).
            body("object.teamName", equalTo(teamDTO.get("teamName"))).
            body("object.captain.profile.displayName", equalTo(captain.getProfile().getDisplayName())).
            body("object.members", hasSize(1));
        //@formatter:on
    }

    @Test
    public void testCreateTeamAsUserDuplicateTeamName() {
        User captain = createUser();
        User captain2 = createUser();
        Map<String, String> teamDTO = getTeamDTO(captain);
        Map<String, String> teamDTO2 = getTeamDTO(captain2);
        teamDTO2.put("teamName", teamDTO.get("teamName"));

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            body(teamDTO).contentType(ContentType.JSON).
            post(TEAM_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_CREATED).
            header("Location", containsString("/teams/")).
            body("object.teamName", equalTo(teamDTO.get("teamName"))).
            body("object.captain.profile.displayName", equalTo(captain.getProfile().getDisplayName())).
            body("object.members.profile.displayName", hasItem(captain.getProfile().getDisplayName()));

        given().
            header(getXAuthTokenHeaderForUser(captain2)).
        when().
            body(teamDTO2).contentType(ContentType.JSON).
            post(TEAM_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on
    }

    @Test
    public void testCreateTeamAsUserDuplicateTeamNameDifferentCasing() {
        User captain = createUser();
        Map<String, String> teamDTO = getTeamDTO(captain);
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            body(teamDTO).contentType(ContentType.JSON).
            post(TEAM_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_CREATED).
            header("Location", containsString("/teams/")).
            body("object.teamName", equalTo(teamDTO.get("teamName"))).
            body("object.captain.profile.displayName", equalTo(captain.getProfile().getDisplayName())).
            body("object.members.profile.displayName", hasItem(captain.getProfile().getDisplayName()));

        Map<String, String> teamDTO2 = getTeamDTO(user);
        teamDTO2.put("teamName", teamDTO.get("teamName").toUpperCase());

        given().
                header(getXAuthTokenHeaderForUser(user)).
                when().
                body(teamDTO).contentType(ContentType.JSON).
                post(TEAM_ENDPOINT).
                then().
                statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on
    }
    //endregion

    //region Test Get Team
    @Test
    public void getTeamAsAdmin() {
        User admin = createAdmin();
        User captain = createUser();
        Team team = createTeamWithCaptain(captain);
        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            get(TEAM_ENDPOINT + team.getId()).
        then().
            statusCode(HttpStatus.SC_OK);
        //formatter:on
    }

    @Test
    public void getTeamAsCaptain() {
        User captain = createUser();
        Team team = createTeamWithCaptain(captain);
        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            get(TEAM_ENDPOINT + team.getId()).
        then().
            statusCode(HttpStatus.SC_OK);
        //formatter:on
    }

    @Test
    public void getTeamAsMember() {
        User captain = createUser();
        User member = createUser();
        Team team = createTeamWithCaptain(captain);
        team = addMemberToTeam(team, member);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(member)).
        when().
            get(TEAM_ENDPOINT + team.getId()).
        then().
            statusCode(HttpStatus.SC_OK);
        //formatter:on
    }

    @Test
    public void getTeamAsUser() {
        User captain = createUser();
        User user = createUser();
        Team team = createTeamWithCaptain(captain);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(TEAM_ENDPOINT + team.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //formatter:on
    }

    @Test
    public void getTeamCurrentUser() {
        User captain = createUser();
        Team team = createTeamWithCaptain(captain);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            get("/users/current/teams").
        then().
            statusCode(HttpStatus.SC_OK).
            body("[0].teamName", equalTo(team.getTeamName())).
            body("[0].captain.profile.displayName", equalTo(captain.getProfile().getDisplayName())).
            body("[0].members.profile.displayName", hasItem(captain.getProfile().getDisplayName()));
        //@formatter:on
    }
    //endregion

    //region Test Add/Invite Members
    @Test
    public void testInviteMemberAsAdmin() {
        User captain = createUser();
        User admin = createAdmin();
        User member = createUser();
        Team team = createTeamWithCaptain(captain);
        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            body(member.getEmail()).
            post(TEAM_ENDPOINT + team.getId() + "/invites").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Collection<TeamInviteToken> tokens = teamInviteTokenRepository.findByUserEmailIgnoreCase(member.getEmail());
        assertFalse(tokens.isEmpty());
    }

    @Test
    public void testAddMemberAsAdmin() {
        User captain = createUser();
        User admin = createAdmin();
        User member = createUser();
        Team team = createTeamWithCaptain(captain);

        //@formatter:off
        Header header = getXAuthTokenHeaderForUser(admin);

        given().
            header(header).
        when().
            body(member.getEmail()).
            post(TEAM_ENDPOINT + team.getId()).
        then().
            statusCode(HttpStatus.SC_OK);

        given().
            header(header).
        when().
            get(TEAM_ENDPOINT + team.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("members.profile.displayName", hasItems(
                    captain.getProfile().getDisplayName(),
                    member.getProfile().getDisplayName())).
            body("size", equalTo(2));
        //@formatter:on
    }

    @Test
    public void testInviteMemberAsCaptain() {
        User captain = createUser();
        User member = createUser();
        Team team = createTeamWithCaptain(captain);


        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            body(member.getEmail()).
            post(TEAM_ENDPOINT + team.getId() + "/invites").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Collection<TeamInviteToken> tokens = teamInviteTokenRepository.findByUserEmailIgnoreCase(member.getEmail());
        assertFalse(tokens.isEmpty());
    }

    @Test
    public void testInviteMemberTwiceAsCaptain() {
        User captain = createUser();
        User user = createUser();
        Team team = createTeamWithCaptain(captain);

        //@formatter:off
        Header header = getXAuthTokenHeaderForUser(captain);

        given().
            header(header).
        when().
            body(user.getEmail()).
            post(TEAM_ENDPOINT + team.getId() + "/invites").
        then().
            statusCode(HttpStatus.SC_OK);

        given().
            header(header).
        when().
            body(user.getEmail()).
            post(TEAM_ENDPOINT + team.getId() + "/invites").
        then().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on

        Collection<TeamInviteToken> tokens = teamInviteTokenRepository.findByUserEmailIgnoreCase(user.getEmail());
        assertEquals(1, tokens.size());
    }

    @Test
    public void testAddMemberAsMember() {
        User captain = createUser();
        Team team = createTeamWithCaptain(captain);
        User member = createUser();
        User member2 = createUser();
        team = addMemberToTeam(team, member);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(member)).
        when().
            body(member2.getEmail()).
            post(TEAM_ENDPOINT + team.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testInviteMemberAsMember() {
        User captain = createUser();
        Team team = createTeamWithCaptain(captain);
        User member = createUser();
        User member2 = createUser();
        team = addMemberToTeam(team, member);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(member)).
        when().
            body(member2.getEmail()).
            post(TEAM_ENDPOINT + team.getId() + "/invites").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAddMemberAsUser() {
        User captain = createUser();
        Team team = createTeamWithCaptain(captain);
        User member = createUser();
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            body(member.getEmail()).
            post(TEAM_ENDPOINT + team.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testInviteMemberAsUser() {
        User captain = createUser();
        Team team = createTeamWithCaptain(captain);
        User member = createUser();
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            body(member.getEmail()).
            post(TEAM_ENDPOINT + team.getId() + "/invites").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAddSelfToTeamAsCaptain() {
        User captain = createUser();
        Team team = createTeamWithCaptain(captain);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            body(captain.getEmail()).
            post(TEAM_ENDPOINT + team.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testInviteSelfToTeamAsCaptain() {
        User captain = createUser();
        Team team = createTeamWithCaptain(captain);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            body(captain.getEmail()).
            post(TEAM_ENDPOINT + team.getId() + "/invites").
        then().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on
    }

    @Test
    public void testInviteMemberAsCaptainDuplicate() {
        User captain = createUser();
        Team team = createTeamWithCaptain(captain);
        User member = createUser();
        team = addMemberToTeam(team, member);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            body(member.getEmail()).
            post(TEAM_ENDPOINT + team.getId() + "/invites").
        then().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on
    }

    @Test
    public void testInviteMemberWithoutTicket() {
        User captain = createUser();
        Team team = createTeamWithCaptain(captain);
        User member = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            body(member.getEmail()).
            post(TEAM_ENDPOINT + team.getId() + "/invites").
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("User " + member.getEmail() + " successfully invited to Team " + team.getId()));
        //@formatter:on
    }

    @Test
    public void testAddMemberAsAdminDuplicate() {
        User admin = createAdmin();
        User captain = createUser();
        User member = createUser();
        Team team = createTeamWithCaptain(captain);
        team = addMemberToTeam(team, member);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            body(member.getEmail()).
            post(TEAM_ENDPOINT + team.getId()).
        then().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on
    }
    //endregion

    //region Test Accept View Delete Invites

    @Test
    public void testViewCurrentUserInvites() {
        User captain = createUser();
        User user = createUser();
        Team team = createTeamWithCaptain(captain);
        teamInviteTokenRepository.save(new TeamInviteToken(user, team));

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get("/users/current/teams/invites").
        then().
            statusCode(HttpStatus.SC_OK).
            body("teamName", hasItem(equalTo(team.getTeamName()))).
            body("email", hasItem(equalTo(user.getEmail()))).
            body("$", hasSize(1));
        //@formatter:on
    }

    @Test
    public void testViewTeamInvitesAsCaptain() {
        User captain = createUser();
        User user = createUser();
        Team team = createTeamWithCaptain(captain);
        teamInviteTokenRepository.save(new TeamInviteToken(user, team));

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            get(TEAM_ENDPOINT + team.getId() + "/invites").
        then().
            statusCode(HttpStatus.SC_OK).
            body("teamName", hasItem(equalTo(team.getTeamName()))).
            body("email", hasItem(equalTo(user.getEmail()))).
            body("$", hasSize(1));
        //@formatter:on
    }

    @Test
    public void testViewTeamInvitesAsMember() {
        User captain = createUser();
        User member = createUser();
        User user = createUser();
        Team team = createTeamWithCaptain(captain);
        team = addMemberToTeam(team, member);
        teamInviteTokenRepository.save(new TeamInviteToken(user, team));

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(member)).
        when().
            get(TEAM_ENDPOINT + team.getId() + "/invites").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testViewTeamInvitesAsAdmin() {
        User captain = createUser();
        User admin = createAdmin();
        User user = createUser();
        Team team = createTeamWithCaptain(captain);
        teamInviteTokenRepository.save(new TeamInviteToken(user, team));

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            get(TEAM_ENDPOINT + team.getId() + "/invites").
        then().
            statusCode(HttpStatus.SC_OK).
            body("teamName", hasItem(equalTo(team.getTeamName()))).
            body("email", hasItem(equalTo(user.getEmail()))).
            body("$", hasSize(1));
        //@formatter:on
    }

    @Test
    public void testViewTeamInvitesAsAnon() {
        User captain = createUser();
        User user = createUser();
        Team team = createTeamWithCaptain(captain);
        teamInviteTokenRepository.save(new TeamInviteToken(user, team));

        //@formatter:off
        when().
            get(TEAM_ENDPOINT + team.getId() + "/invites").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAcceptInviteAsUser() {
        User captain = createUser();
        User user = createUser();
        Team team = createTeamWithCaptain(captain);

        TeamInviteToken token = teamInviteTokenRepository.save(new TeamInviteToken(user, team));

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            body(token.getToken()).
            post(TEAM_ENDPOINT + "invites").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Collection<Team> allByMembersEmail = teamRepository.findAllByMembersEmailIgnoreCase(user.getEmail());
        assertFalse(allByMembersEmail.isEmpty());
    }

    @Test
    public void testDeclineInviteAsUser() {
        User captain = createUser();
        User user = createUser();
        Team team = createTeamWithCaptain(captain);

        TeamInviteToken token = teamInviteTokenRepository.save(new TeamInviteToken(user, team));

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            body(token.getToken()).
            delete(TEAM_ENDPOINT + "invites").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Collection<TeamInviteToken> tokens = teamInviteTokenRepository.findByUserEmailIgnoreCase(user.getEmail());
        tokens.removeIf(t -> !t.isValid());

        assertTrue(tokens.isEmpty());
    }

    @Test
    public void testRevokeInviteAsCaptain() {
        User captain = createUser();
        User user = createUser();
        Team team = createTeamWithCaptain(captain);

        TeamInviteToken token = teamInviteTokenRepository.save(new TeamInviteToken(user, team));

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            body(token.getToken()).
            delete(TEAM_ENDPOINT + "invites").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Collection<TeamInviteToken> tokens = teamInviteTokenRepository.findByUserEmailIgnoreCase(user.getEmail());
        tokens.removeIf(t -> !t.isValid());

        assertTrue(tokens.isEmpty());
    }

    @Test
    public void testDeclineInviteAsAnon() {
        User captain = createUser();
        User user = createUser();
        Team team = createTeamWithCaptain(captain);

        TeamInviteToken token = teamInviteTokenRepository.save(new TeamInviteToken(user, team));

        //@formatter:off
        given().
        when().
            body(token.getToken()).
            delete(TEAM_ENDPOINT + "invites").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }


    //endregion

    //region Test Remove Members
    @Test
    public void testRemoveMemberAsMember() {
        User captain = createUser();
        User member = createUser();
        Team team = createTeamWithCaptain(captain);
        team = addMemberToTeam(team, member);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(member)).
        when().
            body(member.getEmail()).
            delete(TEAM_ENDPOINT + team.getId() + "/members").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testRemoveMemberAsCaptain() {
        User captain = createUser();
        User member = createUser();
        Team team = createTeamWithCaptain(captain);
        team = addMemberToTeam(team, member);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            body(member.getEmail()).
            delete(TEAM_ENDPOINT + team.getId() + "/members").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testRemoveMemberAsAnon() {
        User captain = createUser();
        User member = createUser();
        Team team = createTeamWithCaptain(captain);
        team = addMemberToTeam(team, member);

        //@formatter:off
        given().
        when().
            body(member.getEmail()).
            delete(TEAM_ENDPOINT + team.getId() + "/members").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testRemoveMemberAsAdmin() {
        User admin = createAdmin();
        User captain = createUser();
        User member = createUser();
        Team team = createTeamWithCaptain(captain);
        team = addMemberToTeam(team, member);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            body(member.getEmail()).
            delete(TEAM_ENDPOINT + team.getId() + "/members").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testRemoveCaptainAsMember() {
        User captain = createUser();
        User member = createUser();
        Team team = createTeamWithCaptain(captain);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(member)).
        when().
            body(captain.getEmail()).
            delete(TEAM_ENDPOINT + team.getId() + "/members").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testRemoveCaptainAsCaptain() {
        User captain = createUser();
        Team team = createTeamWithCaptain(captain);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            body(captain.getEmail()).
            delete(TEAM_ENDPOINT + team.getId() + "/members").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testRemoveCaptainAsCaptainWithOpenInvite() {
        User captain = createUser();
        User member = createUser();
        Team team = createTeamWithCaptain(captain);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            body(member.getEmail()).
            post(TEAM_ENDPOINT + team.getId() + "/invites").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            body(captain.getEmail()).
            delete(TEAM_ENDPOINT + team.getId() + "/members").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testRemoveCaptainAsAdmin() {
        User admin = createAdmin();
        User captain = createUser();
        Team team = createTeamWithCaptain(captain);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            body(captain.getEmail()).
            delete(TEAM_ENDPOINT + team.getId() + "/members").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        assertNull(teamRepository.findById(team.getId()).orElse(null));
    }

    @Test
    public void testRemoveCaptainFromLargeTeam() {
        User captain = createUser();
        User member = createUser();
        Team team = createTeamWithCaptain(captain);
        team = addMemberToTeam(team, member);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            body(captain.getEmail()).
            delete(TEAM_ENDPOINT + team.getId() + "/members").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", is("Cannot remove captain from team with other members."));
        //@formatter:on
    }

    @Test
    public void testRemoveMemberAsUser() {
        User captain = createUser();
        User member = createUser();
        User user = createUser();
        Team team = createTeamWithCaptain(captain);
        team = addMemberToTeam(team, member);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            body(member.getEmail()).
            delete(TEAM_ENDPOINT + team.getId() + "/members").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testRemoveNonMemberAsCaptain() {
        User captain = createUser();
        User user = createUser();
        Team team = createTeamWithCaptain(captain);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            body(user.getEmail()).
            delete(TEAM_ENDPOINT + team.getId() + "/members").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testRemoveEmptyTeamAsCaptain() {
        User captain = createUser();
        User user = createUser();
        Team team = createTeamWithCaptain(captain);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            body(user.getEmail()).
            delete(TEAM_ENDPOINT + team.getId() + "/members").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testChangeCaptainByCaptain() {
        User captain = createUser();
        User user = createUser();
        Team team = createTeamWithCaptain(captain);
        team.addMember(user);

        //@formatter:off
        given().
                header(getXAuthTokenHeaderForUser(captain)).
                when().
                body(user.getEmail()).
                post(TEAM_ENDPOINT + team.getId() + "/changecaptain").
                then().
                statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Team newTeam = teamRepository.findById(team.getId()).get();
        assertEquals(newTeam.getCaptain().getEmail(), user.getEmail());
    }

    @Test
    public void testChangeCaptainByMember() {
        User captain = createUser();
        User user = createUser();
        Team team = createTeamWithCaptain(captain);
        team.addMember(user);

        //@formatter:off
        given().
                header(getXAuthTokenHeaderForUser(user)).
                when().
                body(user.getEmail()).
                post(TEAM_ENDPOINT + team.getId() + "/changecaptain").
                then().
                statusCode(HttpStatus.SC_UNAUTHORIZED);
        //@formatter:on
    }

    @Test
    public void testChangeCaptainToCaptain() {
        User captain = createUser();
        Team team = createTeamWithCaptain(captain);

        //@formatter:off
        given().
                header(getXAuthTokenHeaderForUser(captain)).
                when().
                body(captain.getEmail()).
                post(TEAM_ENDPOINT + team.getId() + "/changecaptain").
                then().
                statusCode(HttpStatus.SC_NOT_MODIFIED);
        //@formatter:on
    }

    @Test
    public void testChangeCaptainByAdmin() {
        User captain = createUser();
        User user = createUser();
        User admin = createAdmin();

        Team team = createTeamWithCaptain(captain);
        team.addMember(user);

        //@formatter:off
        given().
                header(getXAuthTokenHeaderForUser(admin)).
                when().
                body(user.getEmail()).
                post(TEAM_ENDPOINT + team.getId() + "/changecaptain").
                then().
                statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Team newTeam = teamRepository.findById(team.getId()).get();
        assertEquals(newTeam.getCaptain().getEmail(), user.getEmail());
    }

    @Test
    public void changeCaptainByNonMember() {
        User captain = createUser();
        User user = createUser();
        Team team = createTeamWithCaptain(captain);

        //@formatter:off
        given().
                header(getXAuthTokenHeaderForUser(user)).
                when().
                body(user.getEmail()).
                post(TEAM_ENDPOINT + team.getId() + "/changecaptain").
                then().
                statusCode(HttpStatus.SC_UNAUTHORIZED);
        //@formatter:on
    }

    //endregion

}
