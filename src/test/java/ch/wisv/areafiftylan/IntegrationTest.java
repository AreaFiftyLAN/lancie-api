package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Gender;
import ch.wisv.areafiftylan.model.util.Role;
import ch.wisv.areafiftylan.service.repository.UserRepository;
import ch.wisv.areafiftylan.util.SessionData;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.filter.session.SessionFilter;
import com.jayway.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.config.RedirectConfig.redirectConfig;
import static com.jayway.restassured.config.RestAssuredConfig.config;

/**
 * Created by sille on 12-11-15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ApplicationTest.class)
@WebIntegrationTest("server.port=0")
@ActiveProfiles("test")
public abstract class IntegrationTest {
    @Value("${local.server.port}")
    int port;

    @Autowired
    protected UserRepository userRepository;

    protected User user;

    protected User admin;

    SessionFilter sessionFilter = new SessionFilter();

    @Before
    public void initIntegrationTest() {
        userRepository.deleteAll();

        user = new User("user", new BCryptPasswordEncoder().encode("password"), "user@mail.com");
        user.getProfile()
                .setAllFields("Jan", "de Groot", "MonsterKiller9001", Gender.MALE, "Mekelweg 4", "2826CD", "Delft",
                        "0906-0666", null);

        admin = new User("admin", new BCryptPasswordEncoder().encode("password"), "bert@mail.com");
        admin.addRole(Role.ROLE_ADMIN);
        admin.getProfile()
                .setAllFields("Bert", "Kleijn", "ILoveZombies", Gender.OTHER, "Mekelweg 20", "2826CD", "Amsterdam",
                        "0611", null);

        userRepository.saveAndFlush(user);
        userRepository.saveAndFlush(admin);

        RestAssured.port = port;
        RestAssured.config = config().redirect(redirectConfig().followRedirects(false));
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @After
    public void tearDownIntegrationTest() {
        logout();
        userRepository.deleteAll();
        RestAssured.reset();
    }

    protected SessionData login(String username) {
        return login(username, "password");
    }


    protected SessionData login(String username, String password) {
        //@formatter:off
        Response getLoginResponse =
            given().
                filter(sessionFilter).
            when().
                get("/login").
            then().
                extract().response();
        //@formatter:on

        String token = getLoginResponse.header("X-CSRF-TOKEN");

        //@formatter:off
        given().
            filter(sessionFilter).
            param("username", username).
            param("password", password).
            param("_csrf", token).
        when().
            post("/login");

        Response tokenResponse =
            given().
                filter(sessionFilter).
            when().
                get("/token").
            then().
                extract().response();
        //@formatter:on

        return new SessionData(tokenResponse.header("X-CSRF-TOKEN"), sessionFilter.getSessionId());
    }

    protected void logout() {
        //@formatter:off
        Response getLoginResponse =
            given().
                filter(sessionFilter).
            when().
                get("/login").
            then().
                extract().response();

        String token = getLoginResponse.header("X-CSRF-TOKEN");

        given().
            filter(sessionFilter).
            param("_csrf", token).
        when().
            post("/logout").
        then();
        //@formatter:on
    }
}
