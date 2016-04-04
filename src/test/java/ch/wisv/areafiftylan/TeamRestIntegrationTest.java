package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Gender;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.security.token.TeamInviteToken;
import ch.wisv.areafiftylan.service.repository.TeamRepository;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import ch.wisv.areafiftylan.service.repository.token.TeamInviteTokenRepository;
import ch.wisv.areafiftylan.util.SessionData;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItem;


public class TeamRestIntegrationTest extends IntegrationTest {

    protected User teamCaptain;
    private Ticket captainTicket;

    @Autowired
    protected TeamRepository teamRepository;

    @Autowired
    private TeamInviteTokenRepository teamInviteTokenRepository;

    @Autowired
    private TicketRepository ticketRepository;

    private Map<String, String> team1 = new HashMap<>();

    @Before
    public void initTeamTest() {
        teamCaptain = new User("captain", new BCryptPasswordEncoder().encode("password"), "captain@mail.com");
        teamCaptain.getProfile()
                .setAllFields("Captain", "Hook", "PeterPanKiller", Gender.MALE, "High Road 3", "2826ZZ", "Neverland",
                        "0906-0777", null);

        teamCaptain = userRepository.saveAndFlush(teamCaptain);

        Ticket captainTicket = new Ticket(teamCaptain, TicketType.EARLY_FULL, false, false);
        captainTicket.setValid(true);

        Ticket userTicket = new Ticket(user, TicketType.EARLY_FULL, false, false);
        userTicket.setValid(true);

        ticketRepository.save(captainTicket);
        ticketRepository.save(userTicket);

        team1.put("teamName", "testteam1");
    }

    @After
    public void teamTestsCleanup() {
        ticketRepository.deleteAll();
        teamInviteTokenRepository.deleteAll();
        teamRepository.deleteAll();
        userRepository.delete(teamCaptain);
    }

    //region Private Helper Functions
    private void addUserAsAdmin(String location, User user) {
        SessionData sessionData = login("admin", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(sessionData.getCsrfHeader()).
        when().
            content(user.getUsername()).
            post(location).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        logout();
    }

    private void inviteUserAsCaptain(String location, User user) {
        SessionData sessionData = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(sessionData.getCsrfHeader()).
        when().
            content(user.getUsername()).
            post(location + "/invites").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        logout();
    }

    private String createTeamWithCaptain() {
        SessionData sessionData = login("captain", "password");

        team1.put("captainUsername", teamCaptain.getUsername());

        //@formatter:off
        Response response =
            given().
                header(sessionData.getCsrfHeader()).
                filter(sessionFilter).
            when().
                content(team1).contentType(ContentType.JSON).
                post("/teams").
            then().
                extract().response();
        //@formatter:on

        logout();
        return response.header("Location");
    }

    private Response getTeam(String location, String user, String password) {
        SessionData login = login(user, password);

        //@formatter:off
        return
            given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
            when().
                get(location).
            then().extract().response();
        //@formatter:on
    }
    //endregion

    //region Test Create Teams
    @Test
    public void testCreateTeamAsCaptain() {
        team1.put("captainUsername", teamCaptain.getUsername());

        SessionData login = login("captain", "password");

        //@formatter:off
        Integer teamId =
            given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
            when().
                content(team1).contentType(ContentType.JSON).
                post("/teams").
            then().
                statusCode(HttpStatus.SC_CREATED).
                header("Location", containsString("/teams/")).
                body("object.teamName", equalTo(team1.get("teamName"))).
                body("object.captain.profile.displayName", equalTo(teamCaptain.getProfile().getDisplayName())).
                body("object.members", hasSize(1)).
            extract().response().path("object.id");
        //@formatter:on

        Team team = teamRepository.getOne(new Long(teamId));
        Assert.assertNotNull(team);
    }

    @Test
    public void testCreateTeamMissingTicket() {
        ticketRepository.deleteAll();
        team1.put("captainUsername", teamCaptain.getUsername());

        SessionData login = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team1).contentType(ContentType.JSON).
            post("/teams").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testCreateTeamAsUserMissingCaptainParameter() {

        SessionData login = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team1).contentType(ContentType.JSON).
            post("/teams").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testCreateTeamWithDifferentCaptainUsername() {
        team1.put("captainUsername", user.getUsername());

        SessionData login = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team1).contentType(ContentType.JSON).
            post("/teams").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testCreateTeamAsAdminWithDifferentCaptain() {
        team1.put("captainUsername", teamCaptain.getUsername());

        SessionData login = login("admin", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team1).contentType(ContentType.JSON).
            post("/teams").
        then().
            statusCode(HttpStatus.SC_CREATED).
            header("Location", containsString("/teams/")).
            body("object.teamName", equalTo(team1.get("teamName"))).
            body("object.captain.profile.displayName", equalTo(teamCaptain.getProfile().getDisplayName())).
            body("object.members", hasSize(1));
        //@formatter:on
    }

    @Test
    public void testCreateTeamAsUserDuplicateTeamName() {
        team1.put("captainUsername", teamCaptain.getUsername());

        SessionData login = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team1).contentType(ContentType.JSON).
            post("/teams").
        then().
            statusCode(HttpStatus.SC_CREATED).
            header("Location", containsString("/teams/")).
            body("object.teamName", equalTo(team1.get("teamName"))).
            body("object.captain.profile.displayName", equalTo(teamCaptain.getProfile().getDisplayName())).
            body("object.members.profile.displayName", hasItem(teamCaptain.getProfile().getDisplayName()));

        logout();

        team1.put("captainUsername", user.getUsername());

        SessionData login2 = login("user", "password");

        given().
            filter(sessionFilter).
            header(login2.getCsrfHeader()).
        when().
            content(team1).contentType(ContentType.JSON).
            post("/teams").
        then().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on
    }
    //endregion

    //region Test Get Team
    @Test
    public void getTeamAsAdmin() {
        Response team = getTeam(createTeamWithCaptain(), "admin", "password");

        team.then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void getTeamAsCaptain() {
        Response team = getTeam(createTeamWithCaptain(), "captain", "password");

        team.then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void getTeamAsMember() {
        String location = createTeamWithCaptain();
        addUserAsAdmin(location, user);
        Response team = getTeam(location, "user", "password");

        team.then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void getTeamAsUser() {
        String location = createTeamWithCaptain();
        Response team = getTeam(location, "user", "password");

        team.then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void getTeamCurrentUser() {
        createTeamWithCaptain();

        SessionData login = login("captain");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/users/current/teams").
        then().
            statusCode(HttpStatus.SC_OK).
            body("[0].teamName", equalTo(team1.get("teamName"))).
            body("[0].captain.profile.displayName", equalTo(teamCaptain.getProfile().getDisplayName())).
            body("[0].members.profile.displayName", hasItem(teamCaptain.getProfile().getDisplayName()));
        //@formatter:on
    }
    //endregion

    //region Test Add/Invite Members
    @Test
    public void testInviteMemberAsAdmin() {
        //@formatter:off
        String location = createTeamWithCaptain();

        SessionData sessionData = login("admin", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(sessionData.getCsrfHeader()).
        when().
            content(user.getUsername()).
            post(location + "/invites").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Collection<TeamInviteToken> tokens = teamInviteTokenRepository.findByUserUsername(user.getUsername());
        Assert.assertFalse(tokens.isEmpty());
    }

    @Test
    public void testAddMemberAsAdmin() {

        //@formatter:off
        String location = createTeamWithCaptain();

        SessionData login = login("admin", "password");

        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(user.getUsername()).
            post(location).
        then().
            statusCode(HttpStatus.SC_OK);

        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get(location).
        then().
            statusCode(HttpStatus.SC_OK).
            body("members.profile.displayName", hasItems(
                    teamCaptain.getProfile().getDisplayName(),
                    user.getProfile().getDisplayName())).
            body("size", equalTo(2));
        //@formatter:on
    }

    @Test
    public void testInviteMemberAsCaptain() {
        //@formatter:off
        String location = createTeamWithCaptain();

        SessionData sessionData = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(sessionData.getCsrfHeader()).
        when().
            content(user.getUsername()).
            post(location + "/invites").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Collection<TeamInviteToken> tokens = teamInviteTokenRepository.findByUserUsername(user.getUsername());
        Assert.assertFalse(tokens.isEmpty());
    }

    @Test
    public void testInviteMemberTwiceAsCaptain() {
        //@formatter:off
        String location = createTeamWithCaptain();

        inviteUserAsCaptain(location, user);

        SessionData sessionData = login("captain", "password");

        given().
            filter(sessionFilter).
            header(sessionData.getCsrfHeader()).
        when().
            content(user.getUsername()).
            post(location + "/invites").
        then().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on

        Collection<TeamInviteToken> tokens = teamInviteTokenRepository.findByUserUsername(user.getUsername());
        Assert.assertEquals(1, tokens.size());
    }

    @Test
    public void testAddMemberAsMember() {
        team1.put("captainUsername", teamCaptain.getUsername());

        //@formatter:off
        String location = createTeamWithCaptain();

        addUserAsAdmin(location, user);

        SessionData login = login("user", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(admin.getUsername()).
            post(location).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testInviteMemberAsMember() {
        team1.put("captainUsername", teamCaptain.getUsername());

        //@formatter:off
        String location = createTeamWithCaptain();

        addUserAsAdmin(location, user);

        SessionData login = login("user", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(admin.getUsername()).
            post(location + "/invites").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAddMemberAsUser() {
        //@formatter:off
        String location = createTeamWithCaptain();

        SessionData login = login("user", "password");

        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(user.getUsername()).
            post(location).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testInviteMemberAsUser() {
        //@formatter:off
        String location = createTeamWithCaptain();

        SessionData login = login("user", "password");

        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(user.getUsername()).
            post(location + "/invites").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAddSelfToTeamAsCaptain() {
        String location = createTeamWithCaptain();

        SessionData login = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(teamCaptain.getUsername()).
            post(location).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testInviteSelfToTeamAsCaptain() {
        String location = createTeamWithCaptain();

        SessionData login = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(teamCaptain.getUsername()).
            post(location + "/invites").
        then().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on
    }

    @Test
    public void testInviteMemberAsCaptainDuplicate() {
        String location = createTeamWithCaptain();

        addUserAsAdmin(location, user);

        SessionData login = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(user.getUsername()).
            post(location + "/invites").
        then().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on
    }

    @Test
    public void testInviteMemberWithoutTicket() {
        String location = createTeamWithCaptain();

        SessionData login = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(admin.getUsername()).
            post(location + "/invites").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAddMemberAsAdminDuplicate() {
        String location = createTeamWithCaptain();

        SessionData login = login("admin", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(user.getUsername()).
            post(location).
        then().
            statusCode(HttpStatus.SC_OK);

        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(user.getUsername()).
            post(location).
        then().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on
    }
    //endregion

    //region Test Accept View Delete Invites

    @Test
    public void testViewCurrentUserInvites() {
        String location = createTeamWithCaptain();

        inviteUserAsCaptain(location, user);

        SessionData login = login("user", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/users/current/teams/invites").
        then().
            statusCode(HttpStatus.SC_OK).
            body("teamName", hasItem(equalTo(team1.get("teamName")))).
            body("username", hasItem(equalTo(user.getUsername()))).
            body("$", hasSize(1));
        //@formatter:on
    }

    @Test
    public void testViewTeamInvitesAsCaptain() {
        String location = createTeamWithCaptain();

        inviteUserAsCaptain(location, user);

        SessionData login = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get(location + "/invites").
        then().
            statusCode(HttpStatus.SC_OK).
            body("teamName", hasItem(equalTo(team1.get("teamName")))).
            body("username", hasItem(equalTo(user.getUsername()))).
            body("$", hasSize(1));
        //@formatter:on
    }

    @Test
    public void testViewTeamInvitesAsMember() {
        String location = createTeamWithCaptain();

        addUserAsAdmin(location, user);

        Ticket adminTicket = new Ticket(admin, TicketType.EARLY_FULL, false, false);
        adminTicket.setValid(true);
        ticketRepository.save(adminTicket);

        inviteUserAsCaptain(location, admin);

        SessionData login = login("user", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get(location + "/invites").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testViewTeamInvitesAsAdmin() {
        String location = createTeamWithCaptain();

        inviteUserAsCaptain(location, user);

        SessionData login = login("admin", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get(location + "/invites").
        then().
            statusCode(HttpStatus.SC_OK).
            body("teamName", hasItem(equalTo(team1.get("teamName")))).
            body("username", hasItem(equalTo(user.getUsername()))).
            body("$", hasSize(1));
        //@formatter:on
    }

    @Test
    public void testViewTeamInvitesAsAnon() {
        String location = createTeamWithCaptain();

        inviteUserAsCaptain(location, user);

        //@formatter:off
        given().
        when().
            get(location + "/invites").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAcceptInviteAsUser() {
        String location = createTeamWithCaptain();

        inviteUserAsCaptain(location, user);

        TeamInviteToken token =
                teamInviteTokenRepository.findByUserUsername(user.getUsername()).stream().findFirst().get();

        SessionData login = login("user", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(token.getToken()).
            post("/teams/invites").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Collection<Team> allByMembersUsername = teamRepository.findAllByMembersUsername(user.getUsername());
        Assert.assertFalse(allByMembersUsername.isEmpty());
    }

    @Test
    public void testDeclineInviteAsUser() {
        String location = createTeamWithCaptain();

        inviteUserAsCaptain(location, user);

        TeamInviteToken token =
                teamInviteTokenRepository.findByUserUsername(user.getUsername()).stream().findFirst().get();

        SessionData login = login("user", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(token.getToken()).
            delete("/teams/invites").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Collection<TeamInviteToken> tokens = teamInviteTokenRepository.findByUserUsername(user.getUsername());

        Assert.assertTrue(tokens.isEmpty());
    }

    //endregion

    //region Test Remove Members
    @Test
    public void testRemoveMemberAsCaptain() {
        String location = createTeamWithCaptain();

        addUserAsAdmin(location, user);

        SessionData login = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(user.getUsername()).delete(location + "/members").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testRemoveCaptainAsCaptain() {
        String location = createTeamWithCaptain();

        addUserAsAdmin(location, user);

        SessionData login = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(teamCaptain.getUsername()).
            delete(location + "/members").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testRemoveMemberAsdmin() {
        String location = createTeamWithCaptain();

        addUserAsAdmin(location, user);

        SessionData login = login("admin", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(user.getUsername()).delete(location + "/members").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testRemoveSelf() {
        String location = createTeamWithCaptain();

        addUserAsAdmin(location, user);

        SessionData login = login("user", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(user.getUsername()).delete(location + "/members").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testRemoveMemberAsUser() {
        String location = createTeamWithCaptain();

        addUserAsAdmin(location, admin);

        SessionData login = login("user", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(admin.getUsername()).delete(location + "/members").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }
    //endregion
}




