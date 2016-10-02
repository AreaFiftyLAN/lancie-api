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

import ch.wisv.areafiftylan.users.model.Gender;
import ch.wisv.areafiftylan.users.model.Role;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.service.UserRepository;
import ch.wisv.areafiftylan.utils.SessionData;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.filter.session.SessionFilter;
import com.jayway.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.config.RedirectConfig.redirectConfig;
import static com.jayway.restassured.config.RestAssuredConfig.config;

/**
 * Created by sille on 12-11-15.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTest {
    @Value("${local.server.port}")
    int port;

    @Autowired
    protected UserRepository userRepository;

    protected User user;
    protected final String userCleartextPassword = "password";

    protected User admin;
    protected final String adminCleartextPassword = "password";

    protected User outsider;
    protected final String outsiderCleartextPassword = "password";


    SessionFilter sessionFilter = new SessionFilter();

    @Before
    public void initIntegrationTest() {
        userRepository.deleteAll();

        user = makeUser();
        admin = makeAdmin();
        outsider = makeOutsider();

        userRepository.saveAndFlush(user);
        userRepository.saveAndFlush(admin);
        userRepository.saveAndFlush(outsider);

        // The test instance is started on a random port, so you can run and test at the same time.
        // This binds the dynamic port to the test framework so that it works.
        RestAssured.port = port;
        RestAssured.config = config().redirect(redirectConfig().followRedirects(false));
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    private User makeUser() {
        User user = new User("user", new BCryptPasswordEncoder().encode(userCleartextPassword), "user@mail.com");
        user.getProfile()
                .setAllFields("Jan", "de Groot", "MonsterKiller9001", Gender.MALE, "Mekelweg 4", "2826CD", "Delft",
                        "0906-0666", null);

        return user;
    }

    private User makeAdmin() {
        User admin = new User("admin", new BCryptPasswordEncoder().encode(adminCleartextPassword), "bert@mail.com");
        admin.addRole(Role.ROLE_ADMIN);
        admin.getProfile()
                .setAllFields("Bert", "Kleijn", "ILoveZombies", Gender.OTHER, "Mekelweg 20", "2826CD", "Amsterdam",
                        "0611", null);

        return admin;
    }

    private User makeOutsider() {
        User outsider = new User("outsider", new BCryptPasswordEncoder().encode("password"), "outsider@gmail.com");
        outsider.getProfile()
                .setAllFields("Nottin", "Todoeo Witit", "Lookinin", Gender.FEMALE, "LoserStreet 1", "2826GJ", "China",
                        "0906-3928", null);

        return userRepository.saveAndFlush(outsider);
    }

    @After
    public void tearDownIntegrationTest() {
        logout();
        userRepository.deleteAll();
        RestAssured.reset();
    }

    protected SessionData login(String username, String password) {
        //@formatter:off
        Response getLoginResponse =
            given().
                filter(sessionFilter).
            when().
                get("/login").
            then().
                extract().response();
        //@formatter:on

        String token = getLoginResponse.header("X-CSRF-TOKEN");

        //@formatter:off
        given().
            filter(sessionFilter).
            param("username", username).
            param("password", password).
            param("_csrf", token).
        when().
            post("/login");

        Response tokenResponse =
            given().
                filter(sessionFilter).
            when().
                get("/token").
            then().
                extract().response();
        //@formatter:on

        return new SessionData(tokenResponse.header("X-CSRF-TOKEN"), sessionFilter.getSessionId());
    }

    protected void logout() {
        //@formatter:off
        Response getLoginResponse =
            given().
                filter(sessionFilter).
            when().
                get("/login").
            then().
                extract().response();

        String token = getLoginResponse.header("X-CSRF-TOKEN");

        given().
            filter(sessionFilter).
            param("_csrf", token).
        when().
            post("/logout").
        then();
        //@formatter:on
    }
}
