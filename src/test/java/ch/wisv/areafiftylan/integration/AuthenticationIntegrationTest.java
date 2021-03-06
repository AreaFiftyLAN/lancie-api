/*
 * Copyright (c) 2018  W.I.S.V. 'Christiaan Huygens'
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

package ch.wisv.areafiftylan.integration;

import ch.wisv.areafiftylan.security.JsonLoginFilter;
import ch.wisv.areafiftylan.security.token.AuthenticationToken;
import ch.wisv.areafiftylan.security.token.repository.AuthenticationTokenRepository;
import ch.wisv.areafiftylan.users.model.User;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationIntegrationTest extends XAuthIntegrationTest {

    @Autowired
    AuthenticationTokenRepository authenticationTokenRepository;

    private User user;
    Map<String, String> userDTO;
    private final String AUTH_HEADER = "X-Auth-Token";

    @BeforeEach
    public void setup() {
        user = createUser();
        userDTO = new HashMap<>();
        userDTO.put("email", user.getEmail());
        userDTO.put("password", cleartextPassword);
    }

    @AfterEach
    public void cleanupAuthTest() {
        authenticationTokenRepository.deleteAll();
    }

    @Test
    public void testRequestToken() {
        //@formatter:off
        Response response = given().
                header("Origin", "rest-assured").
        when().
            body(userDTO).contentType(ContentType.JSON).
            post("/login");

        List<AuthenticationToken> authenticationToken =
                authenticationTokenRepository.findByUserEmailOrderByExpiryDate(user.getEmail());

        assertNotNull(authenticationToken.get(0));

        response.then().
                statusCode(HttpStatus.SC_OK).
                header("X-Auth-Token", containsString(authenticationToken.get(0).getToken())).
                header("Access-Control-Allow-Origin", "*").
                header("Access-Control-Expose-Headers", "X-Auth-Token");
        //@formatter:on
    }

    @Test
    public void testRequestTokenRenew() {
        callLoginOK(userDTO);

        List<AuthenticationToken> authenticationTokenList1 =
                authenticationTokenRepository.findByUserEmailOrderByExpiryDate(user.getEmail());
        assertEquals(1, authenticationTokenList1.size());
        AuthenticationToken authenticationToken1 = authenticationTokenList1.get(0);

        callLoginOK(userDTO);

        List<AuthenticationToken> authenticationTokenList2 =
                authenticationTokenRepository.findByUserEmailOrderByExpiryDate(user.getEmail());

        assertEquals(2, authenticationTokenList2.size());
        AuthenticationToken authenticationToken2 = authenticationTokenList2.get(1);
        assertFalse(authenticationToken1.equals(authenticationToken2));
    }

    @Test
    public void testRequestTokenManySessions() {
        callLoginOK(userDTO);
        List<AuthenticationToken> authenticationTokens =
                authenticationTokenRepository.findByUserEmailOrderByExpiryDate(user.getEmail());

        AuthenticationToken token = authenticationTokens.get(0);
        for (int i = 0; i < 4; i++) {
            callLoginOK(userDTO);
        }

        List<AuthenticationToken> authenticationTokenList =
                authenticationTokenRepository.findByUserEmailOrderByExpiryDate(user.getEmail());

        assertFalse(authenticationTokenList.contains(token));
        assertEquals(4, authenticationTokenList.size());
    }

    @Test
    public void testRequestTokenManySessionsTwoUsers() {
        User user2 = createUser();
        HashMap<String, String> user2DTO = new HashMap<>();
        user2DTO.put("email", user2.getEmail());
        user2DTO.put("password", cleartextPassword);

        callLoginOK(user2DTO);

        for (int i = 0; i < 5; i++) {
            callLoginOK(user2DTO);
        }

        List<AuthenticationToken> authenticationTokenList =
                authenticationTokenRepository.findByUserEmailOrderByExpiryDate(user2.getEmail());

        assertNotNull(authenticationTokenList.get(0));
    }

    @Test
    public void testRequestTokenInvalidRequest() {
        userDTO.remove("password");
        userDTO.put("password_invalid", cleartextPassword);

        //@formatter:off
        given().
        when().
            body(userDTO).contentType(ContentType.JSON).
            post("/login").
        then().
            statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
//            body("message", containsString("Cant read request data"));
        //@formatter:on
    }

    @Test
    public void testVerifyValidToken() {
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
        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get("/users/").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testOptionsRequest() {
        //@formatter:off
        given().
        when()
            .options("/users/").
        then()
            .statusCode(HttpStatus.SC_OK);
        //@formatter:on

    }

    @Test
    public void testLogout() {
        Header xAuthTokenHeaderForUser = getXAuthTokenHeaderForUser(user);
        //@formatter:off
        given().
            header(xAuthTokenHeaderForUser).
        when().
            post("/logout").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        //@formatter:off
        given().
            header(xAuthTokenHeaderForUser).
        when().
            get("/token/verify").
        then().
            statusCode(HttpStatus.SC_UNAUTHORIZED);
        //@formatter:on
    }

    public void failLogins(int n) {
        userDTO.put("password", "!" + cleartextPassword);
        for (int i = 0; i < n; i++) {
            //@formatter:off
            given().
                header("Origin", "rest-assured").
                header("X-Forwarded-For", "10.0.0.1").
            when().
                body(userDTO).contentType(ContentType.JSON).
                post("/login").
            then().
                statusCode(HttpStatus.SC_UNAUTHORIZED);
            //@formatter:on
        }
    }

    @Test
    public void testRateLimitBlock() {
        failLogins(JsonLoginFilter.MAX_ATTEMPTS_MINUTE);

        userDTO.put("password", cleartextPassword);
        //@formatter:off
        given().
            header("Origin", "rest-assured").
            header("X-Forwarded-For", "10.0.0.1").
        when().
            body(userDTO).contentType(ContentType.JSON).
            post("/login").
        then().
            statusCode(HttpStatus.SC_UNAUTHORIZED);
        //@formatter:on
    }

    @Test
    public void testRateLimitInvalidateCache() {
        failLogins(JsonLoginFilter.MAX_ATTEMPTS_MINUTE - 1);

        userDTO.put("password", cleartextPassword);
        for (int i = 0; i < 2; i++) {
            //@formatter:off
            given().
                header("Origin", "rest-assured").
                header("X-Forwarded-For", "10.0.0.1").
            when().
                body(userDTO).contentType(ContentType.JSON).
                post("/login").
            then().
                statusCode(HttpStatus.SC_OK);
            //@formatter:on
        }
    }

    private void callLoginOK(Map<String, String> userDTO) {
        //@formatter:off
        given().
                when().
                body(userDTO).contentType(ContentType.JSON).
                post("/login").
                then().
                statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }
}
