package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Gender;
import ch.wisv.areafiftylan.service.repository.TeamRepository;
import com.jayway.restassured.http.ContentType;
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

        given().auth().form("captain", "password", formAuthConfig).
                when().
                    content(team1).contentType(ContentType.JSON).post("/teams").
                then().log().all().
                    statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    public void testCreateTeam_nonAdmin_differentCaptain() {
        team1.put("captainUsername", user.getUsername());

        given().auth().form("captain", "password", formAuthConfig).
                when().
                    content(team1).contentType(ContentType.JSON).post("/teams").
                then().log().all().
                    statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testCreateTeam_Admin_differentCaptain() {
        team1.put("captainUsername", teamCaptain.getUsername());

        given().auth().form("admin", "password", formAuthConfig).
                when().
                content(team1).contentType(ContentType.JSON).post("/teams").
                then().log().all().
                statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    public void testCreateTeam_nonAdmin_duplicateTeamName() {
        team1.put("captainUsername", teamCaptain.getUsername());

        given().auth().form("captain", "password", formAuthConfig).
                when().
                content(team1).contentType(ContentType.JSON).post("/teams").
                then().log().all().
                statusCode(HttpStatus.SC_CREATED);

        team1.put("captainUsername", user.getUsername());

        given().auth().form("user", "password", formAuthConfig).
                when().
                content(team1).contentType(ContentType.JSON).post("/teams").
                then().log().all().
                statusCode(HttpStatus.SC_CONFLICT);
    }
}




