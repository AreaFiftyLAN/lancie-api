package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.Application;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Gender;
import ch.wisv.areafiftylan.model.util.Role;
import ch.wisv.areafiftylan.service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.authentication.FormAuthConfig;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import static com.jayway.restassured.RestAssured.given;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest("server.port=0")
@ActiveProfiles("test")
public class UserRestIntegrationTest {

    @Value("${local.server.port}")
    int port;

    //Required to Generate JSON content from Java objects
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    //Required to delete the data added for tests.
    //Directly invoke the APIs interacting with the DB
    @Autowired
    private UserRepository userRepository;

    //Test RestTemplate to invoke the APIs.
    private RestTemplate anonRestTemplate = new TestRestTemplate(TestRestTemplate.HttpClientOption.ENABLE_COOKIES);
    private RestTemplate userRestTemplate =
            new TestRestTemplate("user", "password", TestRestTemplate.HttpClientOption.ENABLE_COOKIES);
    private RestTemplate adminRestTemplate =
            new TestRestTemplate("admin", "password", TestRestTemplate.HttpClientOption.ENABLE_COOKIES);

    private String serverPath;

    private User testUser1;

    private User testUser2;

    @Before
    public void init() {
        userRepository.deleteAll();
        serverPath = "http://localhost:" + port;

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

    // USER GET
    // GET ALL USERS AS ANONYMOUS

    @Test
    public void testGetAllUsersAdmin() {
        given().auth().form("admin", "password", new FormAuthConfig("/login", "username", "password")).
                when().get("/users").
                then().statusCode(HttpStatus.SC_OK).;
    }


    // GET ALL USERS AS USER

    // GET ALL USERS AS ADMIN

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




