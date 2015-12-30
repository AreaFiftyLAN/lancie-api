package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.service.repository.VerificationTokenRepository;
import ch.wisv.areafiftylan.util.SessionData;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

public class UserRestIntegrationTest extends IntegrationTest {

    @Autowired
    VerificationTokenRepository verificationTokenRepository;


    @After
    public void cleanupUserTest() {
        verificationTokenRepository.deleteAll();
    }

    private Header getCSRFHeader() {
        //@formatter:off
        Response getLoginResponse =
            given().
                filter(sessionFilter).
            when().
                get("/login").
            then().
                extract().response();
        //@formatter:on

        return new Header("X-CSRF-TOKEN", getLoginResponse.header("X-CSRF-TOKEN"));
    }

    // CHECK AVAILABILITY
    @Test
    public void testUsernameTaken() {

        when().get("/users/checkUsername?username=user").then().body(equalTo("false"));
    }

    @Test
    public void testUsernameFree() {
        when().get("/users/checkUsername?username=freeUsername").then().body(equalTo("true"));
    }

    @Test
    public void testEmailTaken() {
        when().get("/users/checkEmail?email=user@mail.com").then().body(equalTo("false"));
    }

    @Test
    public void testEmailFree() {
        when().get("/users/checkEmail?email=freemail@mail.com").then().body(equalTo("true"));
    }

    @Test
    public void testGetAllUsersAsAnonymous() {
        //@formatter:off
        when().
            get("/users").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetAllUsersAsUser() {
        SessionData login = login("user", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/users").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testGetAllUsersAsAdmin() {
        SessionData login = login("admin", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/users").
        then().
            statusCode(HttpStatus.SC_OK).
            body("username", hasItems(user.getUsername(), admin.getUsername())).
            body("profile.displayName",
            hasItems(user.getProfile().getDisplayName(), admin.getProfile().getDisplayName()));
        //@formatter:on
    }

    @Test
    public void testGetCurrentUserAsAnonymous() {
        //@formatter:off
        when().
            get("/users/current").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetCurrentUserAsUser() {
        SessionData login = login("user", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/users/current").
        then().statusCode(HttpStatus.SC_OK).
            body("username", equalTo(user.getUsername())).
            body("email", equalTo(user.getEmail())).
            body("authorities", hasItem("ROLE_USER"));
        //@formatter:on
    }

    @Test
    public void testGetCurrentUserAsAdmin() {
        SessionData login = login("admin", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/users/current").
        then().statusCode(HttpStatus.SC_OK).
            body("username", equalTo(admin.getUsername())).
            body("email", equalTo(admin.getEmail())).
            body("authorities", hasItem("ROLE_ADMIN"));
        //@formatter:on
    }

    @Test
    public void testGetOtherUserAsAnonymous() {
        //@formatter:off
        when().
            get("/users/1").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetOtherUserAsUser() {
        long id = user.getId();
        id++;
        SessionData login = login("user", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().get("/users/" + id).
            then().statusCode(HttpStatus.SC_FORBIDDEN).body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testGetOtherUserAsAdmin() {
        long userId = user.getId();
        SessionData login = login("admin", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/users/" + userId).
        then().statusCode(HttpStatus.SC_OK).
            body("username", equalTo(user.getUsername())).
            body("email", equalTo(user.getEmail()));
        //@formatter:on
    }

    @Test
    public void testGetOwnUserId() {
        SessionData login = login("user", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/users/" + user.getId()).
        then().statusCode(HttpStatus.SC_OK).
            body("username", equalTo(user.getUsername())).
            body("email", equalTo(user.getEmail()));
        //@formatter:on
    }


    // USER POST
    @Test
    public void createUser() {
        Map<String, String> userDTO = new HashMap<>();
        userDTO.put("username", "testuser");
        userDTO.put("password", "password");
        userDTO.put("email", "test@mail.com");

        //@formatter:off
        given().
            header(getCSRFHeader()).
            filter(sessionFilter).
        when().
            content(userDTO).contentType(ContentType.JSON).
            post("/users").
        then().
            statusCode(HttpStatus.SC_CREATED).
            body("message", containsString("User successfully created at")).
            body("object.username", is(userDTO.get("username"))).
            body("object.email", is(userDTO.get("email")));
        //@formatter:on
    }

    @Test
    public void createUserMissingUsernameField() {
        Map<String, String> userDTO = new HashMap<>();
        userDTO.put("password", "password");
        userDTO.put("email", "test@mail.com");

        //@formatter:off
        given().
            header(getCSRFHeader()).
            filter(sessionFilter).
        when().
            content(userDTO).contentType(ContentType.JSON).
            post("/users").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void createUserEmptyUsernameField() {
        Map<String, String> userDTO = new HashMap<>();
        userDTO.put("username", "");
        userDTO.put("password", "password");
        userDTO.put("email", "test@mail.com");

        //@formatter:off
        given().
            header(getCSRFHeader()).
            filter(sessionFilter).
        when().
            content(userDTO).contentType(ContentType.JSON).
            post("/users").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void createUserMissingEmailField() {
        Map<String, String> userDTO = new HashMap<>();
        userDTO.put("username", "testuser");
        userDTO.put("password", "password");

        //@formatter:off
        given().
            header(getCSRFHeader()).
            filter(sessionFilter).
        when().
            content(userDTO).contentType(ContentType.JSON).
            post("/users").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void createUserEmptyEmailField() {
        Map<String, String> userDTO = new HashMap<>();
        userDTO.put("username", "testuser");
        userDTO.put("password", "password");
        userDTO.put("email", "");

        //@formatter:off
        given().
            header(getCSRFHeader()).
            filter(sessionFilter).
        when().
            content(userDTO).contentType(ContentType.JSON).
            post("/users").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void createUserMissingPasswordField() {
        Map<String, String> userDTO = new HashMap<>();
        userDTO.put("username", "testuser");
        userDTO.put("email", "test@mail.com");

        //@formatter:off
        given().
            header(getCSRFHeader()).
            filter(sessionFilter).
        when().
            content(userDTO).contentType(ContentType.JSON).
            post("/users").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void createUserEmptyPasswordField() {
        Map<String, String> userDTO = new HashMap<>();
        userDTO.put("username", "testuser");
        userDTO.put("password", "");
        userDTO.put("email", "test@mail.com");

        //@formatter:off
        given().
            header(getCSRFHeader()).
            filter(sessionFilter).
        when().
            content(userDTO).contentType(ContentType.JSON).
            post("/users").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void createUserTakenUsername() {
        Map<String, String> userDTO = new HashMap<>();
        userDTO.put("username", "user");
        userDTO.put("password", "password");
        userDTO.put("email", "test@mail.com");

        //@formatter:off
        given().
            header(getCSRFHeader()).
            filter(sessionFilter).
        when().
            content(userDTO).contentType(ContentType.JSON).
            post("/users").
        then().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on
    }

    @Test
    public void createUserTakenEmail() {
        Map<String, String> userDTO = new HashMap<>();
        userDTO.put("username", "testuser");
        userDTO.put("password", "password");
        userDTO.put("email", "user@mail.com");

        //@formatter:off
        given().
            header(getCSRFHeader()).
            filter(sessionFilter).
        when().
            content(userDTO).contentType(ContentType.JSON).
            post("/users").
        then().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on
    }

    // USER PATCH


    // USER PUT


    // USER DELETE


}




