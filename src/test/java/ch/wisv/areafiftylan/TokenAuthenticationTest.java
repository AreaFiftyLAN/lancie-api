/*
 * Copyright (c) 2016  W.I.S.V. 'Christiaan Huygens'
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.security.token.AuthenticationToken;
import ch.wisv.areafiftylan.security.token.repository.AuthenticationTokenRepository;
import ch.wisv.areafiftylan.utils.SessionData;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
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
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by Sille Kamoen on 6-5-16.
 */
public class TokenAuthenticationTest extends IntegrationTest {

    @Autowired
    AuthenticationTokenRepository authenticationTokenRepository;

    private final String AUTH_HEADER = "X-Auth-Token";

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

        return new Header(AUTH_HEADER, token);
    }

    @Test
    public void testRequestToken() {
        Map<String, String> userDTO = new HashMap<>();
        userDTO.put("username", user.getUsername());
        userDTO.put("password", userCleartextPassword);

        //@formatter:off
        Response response = given().
        when().
            content(userDTO).contentType(ContentType.JSON).
            post("/token");

        Optional<AuthenticationToken> authenticationToken =
                authenticationTokenRepository.findByUserUsername(user.getUsername());

        Assert.assertTrue(authenticationToken.isPresent());

        response.then().statusCode(HttpStatus.SC_OK).body("object", containsString(authenticationToken.get().getToken()));

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
    public void testVerifyValidToken() {
        Header header = getTokenHeader(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            header(header).
        when().
            get("/token/verify").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testVerifyInvalidToken() {
        Header header = new Header(AUTH_HEADER, "invalid");

        //@formatter:off
        given().
            header(header).
        when().
            post("/token/verify").
        then().
            statusCode(HttpStatus.SC_UNAUTHORIZED);
        //@formatter:on
    }

    @Test
    public void testVerifyMissingToken() {
        Header header = new Header(AUTH_HEADER, "");

        //@formatter:off
        given().
            header(header).
        when().
            post("/token/verify").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
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

        Header header = new Header(AUTH_HEADER, "hack");

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
