package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Gender;
import ch.wisv.areafiftylan.service.repository.TeamRepository;
import ch.wisv.areafiftylan.service.repository.UserRepository;
import com.jayway.restassured.RestAssured;
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
import static com.jayway.restassured.RestAssured.when;
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
    public void teamTestsCleanup(){
        teamRepository.deleteAll();
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

        System.out.println("Creating first team");
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
            body("object.members", hasSize(1));
        //@formatter:on

        team1.put("captainUsername", user.getUsername());

        System.out.println("Creating 2nd team");
        //@formatter:off
        given().
            auth().form("user", "password", formAuthConfig).
        when().
            content(team1).contentType(ContentType.JSON).post("/teams").
        then().log().ifValidationFails().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on

        Assert.assertEquals(teamRepository.findAll().size(), 1);
    }

/*    @Test
    public void testAddMember() {
        team1.put("captainUsername", teamCaptain.getUsername());

        //@formatter:off
        Response response =
            given().auth().form("captain", "password", formAuthConfig).
            when().content(team1).contentType(ContentType.JSON).post("/teams").
            then().extract().response();
        //@formatter:on

        String location = response.header("Location");
        Integer teamIdInt = response.body().path("object.id");
        Long teamId = new Long(teamIdInt);

        given().auth().form("captain", "password", formAuthConfig).
                when().
                content(user.getUsername()).post(location).
                then().log().all().statusCode(HttpStatus.SC_OK);

        when().get(location).then().log().all().statusCode(HttpStatus.SC_OK);
    }*/
}




