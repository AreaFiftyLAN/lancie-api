package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Gender;
import ch.wisv.areafiftylan.service.repository.TeamRepository;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest("server.port=0")
@ActiveProfiles("test")
public class TeamRestIntegrationTest extends IntegrationTest {

    protected User teamCaptain;

    @Autowired
    private TeamRepository teamRepository;

    private Map<String, String> team1;

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

        //@formatter:off
        Integer teamId =
                given().
                        auth().form("captain", "password", formAuthConfig).
                        when().
                        content(team1).contentType(ContentType.JSON).post("/teams").
                        then().log().all().
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
    public void testCreateTeam_nonAdmin_differentCaptain() {
        team1.put("captainUsername", user.getUsername());

        //@formatter:off
        given().
                auth().form("captain", "password", formAuthConfig).
                when().
                content(team1).contentType(ContentType.JSON).post("/teams").
                then().log().ifValidationFails().
                statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testCreateTeam_Admin_differentCaptain() {
        team1.put("captainUsername", teamCaptain.getUsername());

        //@formatter:off
        given().
                auth().form("admin", "password", formAuthConfig).
                when().
                content(team1).contentType(ContentType.JSON).post("/teams").
                then().log().all().
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

        //@formatter:off
        given().
                auth().form("captain", "password", formAuthConfig).
                when().
                content(team1).contentType(ContentType.JSON).post("/teams").
                then().log().all().
                statusCode(HttpStatus.SC_CREATED).
                header("Location", containsString("/teams/")).
                body("object.teamName", equalTo(team1.get("teamName"))).
                body("object.captain.profile.displayName", equalTo(teamCaptain.getProfile().getDisplayName())).
                body("object.members.profile.displayName", hasItem(teamCaptain.getProfile().getDisplayName()));
        //@formatter:on

        team1.put("captainUsername", user.getUsername());

        //@formatter:off
        given().
            auth().form("user", "password", formAuthConfig).
        when().
            content(team1).contentType(ContentType.JSON).post("/teams").
        then().log().ifValidationFails().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on
    }

    @Test
    public void getTeamAsAdmin(){
        Response team = getTeam(createTeamWithCaptain(), "admin", "password");

        team.then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void getTeamAsCaptain(){
        Response team = getTeam(createTeamWithCaptain(), "captain", "password");

        team.then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void getTeamAsMember(){
        String location = createTeamWithCaptain();
        addUserAsCaptain(location, user);
        Response team = getTeam(location, "user", "password");

        team.then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void getTeamAsUser(){
        String location = createTeamWithCaptain();
        Response team = getTeam(location, "user", "password");

        team.then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    private void addUserAsCaptain(String location, User user) {
        given().
                auth().form("captain", "password", formAuthConfig).
                when().
                content(user.getUsername()).post(location).
                then().log().ifValidationFails().statusCode(HttpStatus.SC_OK);
    }

    private String createTeamWithCaptain() {
        team1.put("captainUsername", teamCaptain.getUsername());

        return given().
                auth().form("captain", "password", formAuthConfig).
                when().
                content(team1).contentType(ContentType.JSON).post("/teams").
                then().log().all().extract().response().header("Location");
    }

    private Response getTeam(String location, String user, String password) {
        return given().
                auth().form(user, password, formAuthConfig).
                when().
                get(location).
                then().log().all().extract().response();
    }

    @Test
    public void testAddMember_as_Admin() {

        //@formatter:off
        String location = createTeamWithCaptain();

        given().
            auth().form("admin", "password", formAuthConfig).
        when().
            content(user.getUsername()).post(location).
        then().log().ifValidationFails()
            .statusCode(HttpStatus.SC_OK);

        given().
            auth().form("admin", "password", formAuthConfig).
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
    public void testAddMember_as_Captain() {
        team1.put("captainUsername", teamCaptain.getUsername());

        //@formatter:off
        String location = createTeamWithCaptain();

        addUserAsCaptain(location, user);

        given().
            auth().form("captain", "password", formAuthConfig).
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

        given().
            auth().form("user", "password", formAuthConfig).
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

        given().
            auth().form("user", "password", formAuthConfig).
        when().
            content(user.getUsername()).post(location).
        then().log().ifValidationFails()
            .statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAddMember_captain() {
        String location = createTeamWithCaptain();

        //@formatter:off
        given().
            auth().form("captain", "password", formAuthConfig).
        when().
            content(teamCaptain.getUsername()).post(location).
        then().log().all()
            .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        //@formatter:on
    }

    @Test
    public void testAddMember_duplicate() {
        String location = createTeamWithCaptain();

        //@formatter:off
        given().
            auth().form("captain", "password", formAuthConfig).
        when().
            content(user.getUsername()).post(location).
        then().log().all()
            .statusCode(HttpStatus.SC_OK);

        given().
            auth().form("captain", "password", formAuthConfig).
        when().
            content(user.getUsername()).post(location).
        then().log().all()
            .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        //@formatter:on
    }
}




