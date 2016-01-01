package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.User;
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

    private String createEnabledTestUser() {
        Map<String, String> userDTO = new HashMap<>();
        userDTO.put("username", "testuser");
        userDTO.put("password", "password");
        userDTO.put("email", "testuser@mail.com");

        //@formatter:off
        Response response = given().log().all().
            header(getCSRFHeader()).
            filter(sessionFilter).
        when().
            content(userDTO).contentType(ContentType.JSON).
            post("/users").
        then().log().all().
            extract().response();
        //@formatter:on

        User testuser = userRepository.findOneByUsername("testuser").get();
        testuser.setEnabled(true);
        userRepository.saveAndFlush(testuser);

        return response.getHeader("Location");
    }

    private Map<String, String> getProfileDTO() {
        Map<String, String> profileDTO = new HashMap<>();
        profileDTO.put("gender", "MALE");
        profileDTO.put("address", "Testaddress");
        profileDTO.put("zipcode", "Testzipcode");
        profileDTO.put("city", "Testcity");
        profileDTO.put("phoneNumber", "TestphoneNumber");
        profileDTO.put("notes", "Testnotes");
        profileDTO.put("firstName", "TestfirstName");
        profileDTO.put("lastName", "TestlastName");
        profileDTO.put("firstName", "TestfirstName");
        profileDTO.put("displayName", "TestdisplayName");
        return profileDTO;
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

    @Test
    public void createProfileAsCurrentUser() {
        createEnabledTestUser();

        Map<String, String> profileDTO = getProfileDTO();

        SessionData login = login("testuser");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(profileDTO).
            contentType(ContentType.JSON).
            post("/users/current/profile").
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.gender", is("MALE")).
            body("object.address", equalTo("Testaddress")).
            body("object.zipcode", equalTo("Testzipcode")).
            body("object.city", equalTo("Testcity")).
            body("object.phoneNumber", equalTo("TestphoneNumber")).
            body("object.notes", equalTo("Testnotes")).
            body("object.firstName", equalTo("TestfirstName")).
            body("object.lastName", equalTo("TestlastName")).
            body("object.displayName", equalTo("TestdisplayName"));
        //@formatter:on
    }

    @Test
    public void createProfileAsUser() {
        String location = createEnabledTestUser();

        Map<String, String> profileDTO = getProfileDTO();

        SessionData login = login("testuser");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(profileDTO).
            contentType(ContentType.JSON).
            post(location + "/profile").
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.gender", is("MALE")).
            body("object.address", equalTo("Testaddress")).
            body("object.zipcode", equalTo("Testzipcode")).
            body("object.city", equalTo("Testcity")).
            body("object.phoneNumber", equalTo("TestphoneNumber")).
            body("object.notes", equalTo("Testnotes")).
            body("object.firstName", equalTo("TestfirstName")).
            body("object.lastName", equalTo("TestlastName")).
            body("object.displayName", equalTo("TestdisplayName"));
        //@formatter:on
    }

    @Test
    public void createProfileAsAdmin() {
        String location = createEnabledTestUser();

        Map<String, String> profileDTO = getProfileDTO();

        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(profileDTO).
            contentType(ContentType.JSON).
            post(location + "/profile").
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.gender", is("MALE")).
            body("object.address", equalTo("Testaddress")).
            body("object.zipcode", equalTo("Testzipcode")).
            body("object.city", equalTo("Testcity")).
            body("object.phoneNumber", equalTo("TestphoneNumber")).
            body("object.notes", equalTo("Testnotes")).
            body("object.firstName", equalTo("TestfirstName")).
            body("object.lastName", equalTo("TestlastName")).
            body("object.displayName", equalTo("TestdisplayName"));
        //@formatter:on
    }

    @Test
    public void createProfileAsOtherUser() {
        String location = createEnabledTestUser();

        Map<String, String> profileDTO = getProfileDTO();

        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(profileDTO).
            contentType(ContentType.JSON).
            post(location + "/profile").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void createProfileMissingField() {
        createEnabledTestUser();

        Map<String, String> profileDTO = getProfileDTO();
        profileDTO.remove("city");

        SessionData login = login("testuser");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(profileDTO).
            contentType(ContentType.JSON).
            post("/users/current/profile").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void createProfileInvalidGender() {
        createEnabledTestUser();

        Map<String, String> profileDTO = getProfileDTO();
        profileDTO.put("gender", "unknown");

        SessionData login = login("testuser");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(profileDTO).
            contentType(ContentType.JSON).
            post("/users/current/profile").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void createProfileEmptyDisplayName() {
        createEnabledTestUser();

        Map<String, String> profileDTO = getProfileDTO();
        profileDTO.put("displayName", "");

        SessionData login = login("testuser");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(profileDTO).
            contentType(ContentType.JSON).
            post("/users/current/profile").
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.gender", is("MALE")).
            body("object.address", equalTo("Testaddress")).
            body("object.zipcode", equalTo("Testzipcode")).
            body("object.city", equalTo("Testcity")).
            body("object.phoneNumber", equalTo("TestphoneNumber")).
            body("object.notes", equalTo("Testnotes")).
            body("object.firstName", equalTo("TestfirstName")).
            body("object.lastName", equalTo("TestlastName")).
            body("object.displayName", equalTo(""));
        //@formatter:on
    }

    @Test
    public void createProfileEmptyNotes() {
        createEnabledTestUser();

        Map<String, String> profileDTO = getProfileDTO();
        profileDTO.remove("notes");

        SessionData login = login("testuser");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(profileDTO).
            contentType(ContentType.JSON).
            post("/users/current/profile").
        then().
        statusCode(HttpStatus.SC_OK).
                body("object.gender", is("MALE")).
                body("object.address", equalTo("Testaddress")).
                body("object.zipcode", equalTo("Testzipcode")).
                body("object.city", equalTo("Testcity")).
                body("object.phoneNumber", equalTo("TestphoneNumber")).
                body("object.notes", equalTo("")).
                body("object.firstName", equalTo("TestfirstName")).
                body("object.lastName", equalTo("TestlastName")).
                body("object.displayName", equalTo("TestdisplayName"));
        //@formatter:on
    }

    @Test
    public void deleteUserAsAdmin() {
        createEnabledTestUser();

        User testuser = userRepository.findOneByUsername("testuser").get();
        long userId = testuser.getId();

        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            delete("/users/" + userId).
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("User disabled"));
        //@formatter:on

        testuser = userRepository.findOneByUsername("testuser").get();
        assert (!testuser.isAccountNonLocked());
    }

    @Test
    public void deleteUserAsUser() {
        createEnabledTestUser();

        User testuser = userRepository.findOneByUsername("testuser").get();
        long userId = testuser.getId();

        SessionData login = login("testuser");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            delete("/users/" + userId).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on

    }

    @Test
    public void deleteUserAsAnon() {
        createEnabledTestUser();

        User testuser = userRepository.findOneByUsername("testuser").get();
        long userId = testuser.getId();

        //@formatter:off
        when().
            delete("/users/" + userId).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }
}




