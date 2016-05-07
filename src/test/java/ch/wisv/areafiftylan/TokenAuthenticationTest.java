package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.security.token.AuthenticationToken;
import ch.wisv.areafiftylan.service.repository.token.AuthenticationTokenRepository;
import ch.wisv.areafiftylan.util.SessionData;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Header;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by Sille Kamoen on 6-5-16.
 */
public class TokenAuthenticationTest extends IntegrationTest {

    @Autowired
    AuthenticationTokenRepository authenticationTokenRepository;

    @After
    public void cleanupAuthTest() {
        authenticationTokenRepository.deleteAll();
    }

    private Header getTokenHeader(String username, String password) {
        Map<String, String> userDTO = new HashMap<>();
        userDTO.put("username", username);
        userDTO.put("password", password);

        //@formatter:off
        String token =
            given().
            when().
                content(userDTO).contentType(ContentType.JSON).
                post("/token").
            then().
                extract().path("object");
        //@formatter:on

        return new Header("X-Auth-Token", token);
    }

    @Test
    public void testRequestToken() {
        Map<String, String> userDTO = new HashMap<>();
        userDTO.put("username", user.getUsername());
        userDTO.put("password", userCleartextPassword);

        //@formatter:off
        given().
        when().
            content(userDTO).contentType(ContentType.JSON).
            post("/token").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        AuthenticationToken authenticationToken1 =
                authenticationTokenRepository.findByUserUsername(user.getUsername()).orElse(null);

        //@formatter:off
        given().
        when().
            content(userDTO).contentType(ContentType.JSON).
            post("/token").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        AuthenticationToken authenticationToken2 =
                authenticationTokenRepository.findByUserUsername(user.getUsername()).orElse(null);

        Assert.assertFalse(authenticationToken1.equals(authenticationToken2));
    }

    @Test
    public void testRequestTokenRenew() {
        Map<String, String> userDTO = new HashMap<>();
        userDTO.put("username", user.getUsername());
        userDTO.put("password", userCleartextPassword);

        //@formatter:off
        given().
        when().
            content(userDTO).contentType(ContentType.JSON).
            post("/token").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Optional<AuthenticationToken> authenticationToken =
                authenticationTokenRepository.findByUserUsername(user.getUsername());

        Assert.assertTrue(authenticationToken.isPresent());

    }

    @Test
    public void testGetCurrentUserWithToken() {
        Header header = getTokenHeader(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            header(header).
        when().
            get("/users/current").
        then().
            statusCode(HttpStatus.SC_OK).
            body("username", equalTo(user.getUsername())).
            body("email", equalTo(user.getEmail()));
        //@formatter:on
    }

    @Test
    public void testPostRequestWithToken() {
        Header header = getTokenHeader(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            header(header).
        when().
            content(UserRestIntegrationTest.getProfileDTO()).
            contentType(ContentType.JSON).
            post("/users/current/profile").
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.gender", is("MALE")).
            body("object.address", Matchers.equalTo("Testaddress")).
            body("object.zipcode", Matchers.equalTo("Testzipcode")).
            body("object.city", Matchers.equalTo("Testcity")).
            body("object.phoneNumber", Matchers.equalTo("TestphoneNumber")).
            body("object.notes", Matchers.equalTo("Testnotes")).
            body("object.firstName", Matchers.equalTo("TestfirstName")).
            body("object.lastName", Matchers.equalTo("TestlastName")).
            body("object.displayName", Matchers.equalTo("TestdisplayName"));
        //@formatter:on
    }

    @Test
    public void testForbiddenRequestWithToken() {
        Header header = getTokenHeader(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            header(header).
        when().
            get("/users/").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testEvadeCSRFWithTokenHeader() {
        SessionData login = login(user.getUsername(), userCleartextPassword);

        Header header = new Header("X-Auth-Token", "hack");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(header).
        when().
            content(UserRestIntegrationTest.getProfileDTO()).
            contentType(ContentType.JSON).
            post("/users/current/profile").
        then().
            statusCode(HttpStatus.SC_UNAUTHORIZED);
        //@formatter:on

    }
}
