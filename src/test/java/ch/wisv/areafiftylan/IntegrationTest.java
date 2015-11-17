package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Gender;
import ch.wisv.areafiftylan.model.util.Role;
import ch.wisv.areafiftylan.service.repository.UserRepository;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.authentication.FormAuthConfig;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static com.jayway.restassured.config.RedirectConfig.redirectConfig;
import static com.jayway.restassured.config.RestAssuredConfig.config;

/**
 * Created by sille on 12-11-15.
 */
public abstract class IntegrationTest {
    @Value("${local.server.port}")
    int port;

    @Autowired
    protected UserRepository userRepository;

    protected User user;

    protected User admin;

    protected static FormAuthConfig formAuthConfig = new FormAuthConfig("/login", "username", "password");

    @Before
    public void initIntegrationTest() {
        userRepository.deleteAll();

        user = new User("user", new BCryptPasswordEncoder().encode("password"), "user@mail.com");
        user.getProfile()
                .setAllFields("Jan", "de Groot", "MonsterKiller9001", Gender.MALE, "Mekelweg 4", "2826CD", "Delft",
                        "0906-0666", null);

        admin = new User("admin", new BCryptPasswordEncoder().encode("password"), "bert@mail.com");
        admin.addRole(Role.ADMIN);
        admin.getProfile()
                .setAllFields("Bert", "Kleijn", "ILoveZombies", Gender.OTHER, "Mekelweg 20", "2826CD", "Amsterdam",
                        "0611", null);

        userRepository.saveAndFlush(user);
        userRepository.saveAndFlush(admin);

        RestAssured.port = port;
        RestAssured.config = config().redirect(redirectConfig().followRedirects(false));
    }

    @After
    public void tearDownIntegrationTest() {
        userRepository.delete(user);
        userRepository.delete(admin);
        RestAssured.reset();
    }
}
