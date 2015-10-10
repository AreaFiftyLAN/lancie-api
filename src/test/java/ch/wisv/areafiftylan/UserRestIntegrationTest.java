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
import static com.jayway.restassured.config.RedirectConfig.redirectConfig;
import static com.jayway.restassured.config.RestAssuredConfig.config;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
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

    private User user;

    private User admin;

    private static FormAuthConfig formAuthConfig = new FormAuthConfig("/login", "username", "password");

    @Before
    public void init() {
        userRepository.deleteAll();

        user = new User("user", new BCryptPasswordEncoder().encode("password"), "user@mail.com");
        user.getProfile()
                .setAllFields("Jan", "de Groot", "MonsterKiller9001", Gender.MALE, "Mekelweg 4", "2826CD", "Delft",
                        "0906-0666", null);

        admin = new User("admin", new BCryptPasswordEncoder().encode("password"), "bert@mail.com");
        admin.addRole(Role.ADMIN);
        admin.getProfile()
                .setAllFields("Bert", "Kleijn", "ILoveZombies", Gender.OTHER, "Mekelweg 20", "2826CD", "Amsterdam",
                        "0611", null);

        userRepository.saveAndFlush(user);
        userRepository.saveAndFlush(admin);

        RestAssured.port = port;
        RestAssured.config = config().redirect(redirectConfig().followRedirects(false));
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
                then().statusCode(HttpStatus.SC_MOVED_TEMPORARILY).header("location", containsString("/login"));
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
                body("username", hasItems(user.getUsername(), admin.getUsername())).
                body("profile.displayName",
                        hasItems(user.getProfile().getDisplayName(), admin.getProfile().getDisplayName()));
    }

    // GET CURRENT USER AS ANONYMOUS
    @Test
    public void testGetCurrentUserAnonymous() {
        when().get("/users/current").
                then().statusCode(HttpStatus.SC_MOVED_TEMPORARILY).header("location", containsString("/login"));
    }

    // PROFILE
    @Test
    public void testGetCurrentProfileAnonymous() {
        when().get("/users/current/profile").
                then().statusCode(HttpStatus.SC_MOVED_TEMPORARILY).header("location", containsString("/login"));
    }

    // GET CURRENT USER AS USER
    @Test
    public void testGetCurrentUserUser() {
        given().auth().form("user", "password", formAuthConfig).
                when().get("/users/current").
                then().statusCode(HttpStatus.SC_OK).
                body("username", equalTo(user.getUsername())).
                body("email", equalTo(user.getEmail())).
                body("authorities", hasItem("USER"));
    }

    // PROFILE
    @Test
    public void testGetCurrentProfileUser() {
        given().auth().form("user", "password", formAuthConfig).
                when().get("/users/current/profile").
                then().statusCode(HttpStatus.SC_OK).
                body("firstName", equalTo(user.getProfile().getFirstName())).
                body("gender", equalTo(user.getProfile().getGender().toString()));
    }

    // GET CURRENT USER AS ADMIN
    @Test
    public void testGetCurrentUserAdmin() {
        given().auth().form("admin", "password", formAuthConfig).
                when().get("/users/current").
                then().statusCode(HttpStatus.SC_OK).
                body("username", equalTo(admin.getUsername())).
                body("email", equalTo(admin.getEmail())).
                body("authorities", hasItem("ADMIN"));
    }

    // PROFILE
    @Test
    public void testGetCurrentProfileAdmin() {
        given().auth().form("admin", "password", formAuthConfig).
                when().get("/users/current/profile").
                then().statusCode(HttpStatus.SC_OK).
                body("firstName", equalTo(admin.getProfile().getFirstName())).
                body("gender", equalTo(admin.getProfile().getGender().toString()));
    }

    // GET OTHER USER AS ANONYMOUS
    @Test
    public void testGetOtherUserAnonymous() {
        when().get("/users/1").
                then().statusCode(HttpStatus.SC_MOVED_TEMPORARILY).header("location", containsString("/login"));
    }

    @Test
    public void testGetOtherProfileAnonymous() {
        when().get("/users/1/profile").
                then().statusCode(HttpStatus.SC_MOVED_TEMPORARILY).header("location", containsString("/login"));
    }

    // GET OTHER USER AS USER
    @Test
    public void testGetOtherUserUser() {
        long id = user.getId();
        id++;
        given().auth().form("user", "password", formAuthConfig).
                when().get("/users/" + id).
                then().statusCode(HttpStatus.SC_FORBIDDEN).body("message", equalTo("Access denied"));
    }

    // GET OTHER USER AS ADMIN
    @Test
    public void testGetOtherUserAdmin() {
        long userId = user.getId();
        given().auth().form("admin", "password", formAuthConfig).
                when().get("/users/" + userId).
                then().statusCode(HttpStatus.SC_OK).
                body("username", equalTo(user.getUsername())).
                body("email", equalTo(user.getEmail()));
    }

    // GET OWN USER VIA ID
    @Test
    public void testGetOwnUserId(){
        given().auth().form("admin", "password", formAuthConfig).
                when().get("/users/" + user.getId()).
                then().statusCode(HttpStatus.SC_OK).
                body("username", equalTo(user.getUsername())).
                body("email", equalTo(user.getEmail()));
    }

    // USER POST
    // CREATE USER AND VERIFY IN DB


    // USER PATCH


    // USER PUT


    // USER DELETE


}




