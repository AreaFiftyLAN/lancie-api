package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Gender;
import ch.wisv.areafiftylan.model.util.Role;
import ch.wisv.areafiftylan.service.repository.UserRepository;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.authentication.FormAuthConfig;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest("server.port=0")
@ActiveProfiles("test")
public class UserRestIntegrationTest {

    @Value("${local.server.port}")
    int port;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;

    private User testUser2;

    private static FormAuthConfig formAuthConfig = new FormAuthConfig("/login", "username", "password");

    @Before
    public void init() {
        userRepository.deleteAll();

        testUser1 = new User("user", new BCryptPasswordEncoder().encode("password"), "user@mail.com");
        testUser1.getProfile()
                .setAllFields("Jan", "de Groot", "MonsterKiller9001", Gender.MALE, "Mekelweg 4", "2826CD", "Delft",
                        "0906-0666", null);

        testUser2 = new User("admin", new BCryptPasswordEncoder().encode("password"), "bert@mail.com");
        testUser2.addRole(Role.ADMIN);
        testUser2.getProfile()
                .setAllFields("Bert", "Kleijn", "ILoveZombies", Gender.OTHER, "Mekelweg 20", "2826CD", "Amsterdam",
                        "0611", null);

        userRepository.saveAndFlush(testUser1);
        userRepository.saveAndFlush(testUser2);

        RestAssured.port = port;
    }

    @After
    public void tearDown() {
        RestAssured.reset();
        userRepository.deleteAll();
    }

    // USER GET
    // GET ALL USERS AS ANONYMOUS
    @Test
    public void testGetAllUsersAnonymous() {
        when().get("/users").
                then().statusCode(HttpStatus.SC_UNAUTHORIZED).body("message", equalTo("Please log in"));
    }


    // GET ALL USERS AS USER
    @Test
    public void testGetAllUsersUser() {
        given().auth().form("user", "password", formAuthConfig).
                when().get("/users").
                then().statusCode(HttpStatus.SC_FORBIDDEN).body("message", equalTo("Access denied"));
    }

    // GET ALL USERS AS ADMIN
    @Test
    public void testGetAllUsersAdmin() {
        given().auth().form("admin", "password", formAuthConfig).
                when().get("/users").
                then().
                statusCode(HttpStatus.SC_OK).
                body("username", hasItems(testUser1.getUsername(), testUser2.getUsername())).
                body("profile.displayName",
                        hasItems(testUser1.getProfile().getDisplayName(), testUser2.getProfile().getDisplayName()));
    }

    // GET CURRENT USER AS ANONYMOUS
    // PROFILE

    // GET CURRENT USER AS USER

    // PROFILE

    // GET CURRENT USER AS ADMIN
    // PROFILE

    // GET OTHER USER AS ANONYMOUS
    // PROFILE

    // GET OTHER USER AS USER

    // GET OTHER USER AS ADMIN

    // GET OWN USER VIA ID


    // USER POST
    // CREATE USER AND VERIFY IN DB


    // USER PATCH


    // USER PUT


    // USER DELETE


}




