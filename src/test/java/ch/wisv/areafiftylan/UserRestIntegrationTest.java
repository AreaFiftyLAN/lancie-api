package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.util.SessionData;
import org.apache.http.HttpStatus;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

public class UserRestIntegrationTest extends IntegrationTest {

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

    // USER GET
    // GET ALL USERS AS ANONYMOUS
    @Test
    public void testGetAllUsersAnonymous() {
        when().get("/users").
                then().statusCode(HttpStatus.SC_FORBIDDEN);
    }


    // GET ALL USERS AS USER
    @Test
    public void testGetAllUsersUser() {
        SessionData login = login("user", "password");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
                when().get("/users").
                then().statusCode(HttpStatus.SC_FORBIDDEN).body("message", equalTo("Access denied"));
    }

    // GET ALL USERS AS ADMIN
    @Test
    public void testGetAllUsersAdmin() {
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

    // GET CURRENT USER AS ANONYMOUS
    @Test
    public void testGetCurrentUserAnonymous() {
        when().get("/users/current").
                then().log().all().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    // GET CURRENT USER AS USER
    @Test
    public void testGetCurrentUserUser() {

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

    // GET CURRENT USER AS ADMIN
    @Test
    public void testGetCurrentUserAdmin() {

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

    // GET OTHER ROLE_USER AS ANONYMOUS
    @Test
    public void testGetOtherUserAnonymous() {
        when().get("/users/1").
                then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    // GET OTHER ROLE_USER AS ROLE_USER
    @Test
    public void testGetOtherUserUser() {
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

    // GET OTHER ROLE_USER AS ROLE_ADMIN
    @Test
    public void testGetOtherUserAdmin() {
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

    // GET OWN ROLE_USER VIA ID
    @Test
    public void testGetOwnUserId() {
        SessionData login = login("admin", "password");

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
    // CREATE USER AND VERIFY IN DB


    // USER PATCH


    // USER PUT


    // USER DELETE


}




