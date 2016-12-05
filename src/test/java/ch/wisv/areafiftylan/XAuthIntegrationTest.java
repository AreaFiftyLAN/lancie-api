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

import ch.wisv.areafiftylan.security.authentication.AuthenticationService;
import ch.wisv.areafiftylan.security.token.repository.AuthenticationTokenRepository;
import ch.wisv.areafiftylan.users.model.Gender;
import ch.wisv.areafiftylan.users.model.Role;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.service.UserRepository;
import io.restassured.RestAssured;
import io.restassured.config.RedirectConfig;
import io.restassured.http.Header;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class XAuthIntegrationTest {

    @Value("${local.server.port}")
    int port;

    protected final String cleartextPassword = "password";

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected AuthenticationTokenRepository authenticationTokenRepository;

    @Autowired
    private AuthenticationService authenticationService;


    @Before
    public void setXAuthIntegrationTest() {
        RestAssured.port = port;
        RestAssured.config().redirect(RedirectConfig.redirectConfig().followRedirects(false));
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    protected User createUser() {
        return createUser(19, false);
    }

    protected User createUser(boolean admin) {
        return createUser(19, admin);
    }

    protected User createUser(int age, boolean admin) {
        long count = userRepository.count();
        User user = new User(count + "@mail.com", new BCryptPasswordEncoder().encode(cleartextPassword));
        if (admin) {
            user.addRole(Role.ROLE_ADMIN);
        }
        user.getProfile()
                .setAllFields("User", String.valueOf(count), "DisplayName" + count, LocalDate.now().minusYears(age),
                        Gender.MALE, "Mekelweg" + count, "2826CD", "Delft", "0906-0666", null);

        return userRepository.save(user);
    }

    protected Header getXAuthTokenHeaderForUser(User user) {
        return getXAuthTokenHeaderForUser(user, cleartextPassword);
    }

    protected Header getXAuthTokenHeaderForUser(User user, String password) {
        return getXAuthTokenHeaderForUser(user.getUsername(), password);
    }

    protected Header getXAuthTokenHeaderForUser(String username, String password) {
        String authToken = authenticationService.createNewAuthToken(username, password);
        return new Header("X-Auth-Token", authToken);
    }

    protected void removeXAuthToken(Header header) {
        authenticationService.removeAuthToken(header.getValue());
    }
}
