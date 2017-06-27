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
import ch.wisv.areafiftylan.users.model.User;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;

public class TokenAuthenticationTest extends XAuthIntegrationTest {

    @Autowired
    AuthenticationTokenRepository authenticationTokenRepository;

    private final String AUTH_HEADER = "X-Auth-Token";

    @After
    public void cleanupAuthTest() {
        authenticationTokenRepository.deleteAll();
    }

    @Test
    public void testRequestToken() {
        User user = createUser();
        Map<String, String> userDTO = new HashMap<>();
        userDTO.put("email", user.getEmail());
        userDTO.put("password", cleartextPassword);

        //@formatter:off
        Response response = given().
                header("Origin", "rest-assured").
        when().
            body(userDTO).contentType(ContentType.JSON).
            post("/login");

        Optional<AuthenticationToken> authenticationToken =
                authenticationTokenRepository.findByUserEmail(user.getEmail());

        Assert.assertTrue(authenticationToken.isPresent());

        response.then().
                statusCode(HttpStatus.SC_OK).
                header("X-Auth-Token", containsString(authenticationToken.get().getToken())).
                header("Access-Control-Allow-Origin", "rest-assured");
        //@formatter:on
    }

    @Test
    public void testRequestTokenRenew() {
        User user = createUser();
        Map<String, String> userDTO = new HashMap<>();
        userDTO.put("email", user.getEmail());
        userDTO.put("password", cleartextPassword);

        //@formatter:off
        given().
        when().
            body(userDTO).contentType(ContentType.JSON).
            post("/login").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        AuthenticationToken authenticationToken1 =
                authenticationTokenRepository.findByUserEmail(user.getEmail()).orElse(null);

        //@formatter:off
        given().
        when().
            body(userDTO).contentType(ContentType.JSON).
            post("/login").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        AuthenticationToken authenticationToken2 =
                authenticationTokenRepository.findByUserEmail(user.getEmail()).orElse(null);

        Assert.assertFalse(authenticationToken1.equals(authenticationToken2));
    }

    @Test
    public void testVerifyValidToken() {
        User user = createUser();
        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
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
            get("/token/verify").
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
            get("/token/verify").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetCurrentUserWithToken() {
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get("/users/current").
        then().
            statusCode(HttpStatus.SC_OK).
            body("email", equalTo(user.getEmail()));
        //@formatter:on
    }

    @Test
    public void testForbiddenRequestWithToken() {
        User user = createUser();
        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get("/users/").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }
}
