package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Gender;
import ch.wisv.areafiftylan.service.repository.TeamRepository;
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

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItem;


public class TeamRestIntegrationTest extends IntegrationTest {

    protected User teamCaptain;

    @Autowired
    protected TeamRepository teamRepository;

    protected Map<String, String> team1;

    @Before
    public void initTeamTest() {
        teamCaptain = new User("captain", new BCryptPasswordEncoder().encode("password"), "captain@mail.com");
        teamCaptain.getProfile()
                .setAllFields("Captain", "Hook", "PeterPanKiller", Gender.MALE, "High Road 3", "2826ZZ", "Neverland",
                        "0906-0777", null);

        userRepository.saveAndFlush(teamCaptain);

        team1 = new HashMap<>();

        team1.put("teamName", "testteam1");
    }

    @After
    public void teamTestsCleanup() {
        teamRepository.deleteAll();
        userRepository.delete(teamCaptain);
    }

    @Test
    public void testCreateTeam_nonAdmin_correct() {
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
    public void testCreateTeam_nonAdmin_differentCaptain() {
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
    public void testCreateTeam_Admin_differentCaptain() {
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
    public void testCreateTeam_nonAdmin_duplicateTeamName() {
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
        //@formatter:on

        logout();

        team1.put("captainUsername", user.getUsername());

        SessionData login2 = login("user", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login2.getCsrfHeader()).
        when().
            content(team1).contentType(ContentType.JSON).
            post("/teams").
        then().log().ifValidationFails().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on
    }

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
        addUserAsCaptain(location, user);
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
        String location = createTeamWithCaptain();
        logout();

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

    private void addUserAsCaptain(String location, User user) {
        SessionData sessionData = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(sessionData.getCsrfHeader()).
        when().
            content(user.getUsername()).post(location).
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

    @Test
    public void testAddMember_as_Admin() {

        //@formatter:off
        String location = createTeamWithCaptain();

        SessionData login = login("admin", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(user.getUsername()).post(location).
        then().log().ifValidationFails()
            .statusCode(HttpStatus.SC_OK);

        logout();

        SessionData login2 = login("admin", "password");

        //@formatter:off
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
    public void testAddMember_as_Captain() {
        team1.put("captainUsername", teamCaptain.getUsername());

        //@formatter:off
        String location = createTeamWithCaptain();

        addUserAsCaptain(location, user);

        SessionData login = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get(location).
        then().log().ifValidationFails().
            statusCode(HttpStatus.SC_OK).
            body("members.profile.displayName", hasItems(
                    teamCaptain.getProfile().getDisplayName(),
                    user.getProfile().getDisplayName())).
            body("size", equalTo(2));
        //@formatter:on
    }

    @Test
    public void testAddMember_as_Member() {
        team1.put("captainUsername", teamCaptain.getUsername());

        //@formatter:off
        String location = createTeamWithCaptain();

        addUserAsCaptain(location, user);

        SessionData login = login("user", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(admin.getUsername()).post(location).
        then().log().ifValidationFails()
            .statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAddMember_as_User() {
        team1.put("captainUsername", teamCaptain.getUsername());

        //@formatter:off
        String location = createTeamWithCaptain();

        SessionData login = login("user", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(user.getUsername()).post(location).
        then().log().ifValidationFails()
            .statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAddMember_captain() {
        String location = createTeamWithCaptain();

        SessionData login = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(teamCaptain.getUsername()).post(location).
        then().log().all()
            .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        //@formatter:on
    }

    @Test
    public void testAddMember_duplicate() {
        String location = createTeamWithCaptain();

        SessionData login = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(user.getUsername()).post(location).
        then().log().all()
            .statusCode(HttpStatus.SC_OK);

        logout();

        SessionData login2 = login("captain", "password");

        given().
            filter(sessionFilter).
            header(login2.getCsrfHeader()).
        when().
            content(user.getUsername()).post(location).
        then().log().all()
            .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        //@formatter:on
    }

    @Test
    public void testRemoveMember_captain() {
        String location = createTeamWithCaptain();

        addUserAsCaptain(location, user);

        SessionData login = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(user.getUsername()).delete(location + "/members").
        then().log().all()
            .statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testRemoveMember_admin() {
        String location = createTeamWithCaptain();

        addUserAsCaptain(location, user);

        SessionData login = login("captain", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(user.getUsername()).delete(location + "/members").
        then().log().all()
            .statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testRemoveMember_member() {
        String location = createTeamWithCaptain();

        addUserAsCaptain(location, user);

        SessionData login = login("user", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(user.getUsername()).delete(location + "/members").
        then().log().all()
            .statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testRemoveMember_user() {
        String location = createTeamWithCaptain();

        addUserAsCaptain(location, admin);

        SessionData login = login("user", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(admin.getUsername()).delete(location + "/members").
        then().log().all()
            .statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }
}




