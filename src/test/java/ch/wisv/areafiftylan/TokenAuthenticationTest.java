package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.security.token.AuthenticationToken;
import ch.wisv.areafiftylan.service.repository.token.AuthenticationTokenRepository;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Header;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.jayway.restassured.RestAssured.given;
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
            then().extract().path("object");
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
        then().log().all().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Optional<AuthenticationToken> authenticationToken =
                authenticationTokenRepository.findByUserUsername(user.getUsername());

        Assert.assertTrue(authenticationToken.isPresent());
    }

    @Test
    public void testRequestTokenRenew() {

    }

    @Test
    public void testRequestWithToken() {
        Header header = getTokenHeader(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().log().all().
            header(header).
        when().
            get("/users/current").
        then().
            statusCode(HttpStatus.SC_OK).
            body("username", equalTo(user.getUsername()));
        //@formatter:on
    }
}