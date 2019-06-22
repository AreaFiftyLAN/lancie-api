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

import ch.wisv.areafiftylan.ApplicationTest;
import ch.wisv.areafiftylan.exception.TicketOptionNotFoundException;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.model.TicketOption;
import ch.wisv.areafiftylan.products.service.repository.TicketOptionRepository;
import ch.wisv.areafiftylan.products.service.repository.TicketRepository;
import ch.wisv.areafiftylan.products.service.repository.TicketTypeRepository;
import ch.wisv.areafiftylan.security.authentication.AuthenticationService;
import ch.wisv.areafiftylan.security.token.repository.AuthenticationTokenRepository;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.teams.service.TeamRepository;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = ApplicationTest.class)
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

    @Autowired
    protected TicketTypeRepository ticketTypeRepository;
    @Autowired
    private TicketOptionRepository ticketOptionRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private TeamRepository teamRepository;

    protected final String CH_MEMBER = "chMember";
    protected final String PICKUP_SERVICE = "pickupService";
    protected final String TEST_TICKET = "test";

    @Before
    public void setXAuthIntegrationTest() {
        RestAssured.port = port;
        RestAssured.config().redirect(RedirectConfig.redirectConfig().followRedirects(false));
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    protected User createUser() {
        return createUser(19, Role.ROLE_USER);
    }

    protected User createOperator() {
        return createUser(19, Role.ROLE_OPERATOR);
    }

    protected User createCommitteeMember() {
        return createUser(19, Role.ROLE_COMMITTEE);
    }

    protected User createAdmin() {
        return createUser(19, Role.ROLE_ADMIN);
    }

    protected User createUser(int age, Role role) {
        long count = userRepository.count();
        User user = new User(count + "@mail.com", new BCryptPasswordEncoder().encode(cleartextPassword));
        user.addRole(role);
        user.getProfile()
                .setAllFields("User", String.valueOf(count), "DisplayName" + count, LocalDate.now().minusYears(age),
                        Gender.MALE, "Mekelweg" + count, "2826CD", "Delft", "0906-0666", null);

        return userRepository.save(user);
    }

    protected Ticket createTicket(User user, List<String> options) {
        Ticket ticket = new Ticket(user,
                ticketTypeRepository.findByName(TEST_TICKET).orElseThrow(IllegalArgumentException::new));
        options.forEach(o -> ticket.addOption(getTicketOption(o)));
        ticket.setValid(true);

        return ticketRepository.save(ticket);
    }

    protected Ticket createTicketForUser(User user) {
        return createTicket(user, Collections.emptyList());
    }

    private TicketOption getTicketOption(String option) {
        return ticketOptionRepository.findByName(option).orElseThrow(TicketOptionNotFoundException::new);
    }

    protected Team createTeamWithCaptain(User captain) {
        Team team = new Team("Team " + captain.getId(), captain);
        return teamRepository.save(team);
    }

    protected Team addMemberToTeam(Team team, User user) {
        team.addMember(user);
        return teamRepository.save(team);
    }

    protected Header getXAuthTokenHeaderForUser(User user) {
        return getXAuthTokenHeaderForUser(user.getEmail());
    }

    protected Header getXAuthTokenHeaderForUser(String email) {
        String authToken = authenticationService.createNewAuthToken(email);
        return new Header("X-Auth-Token", authToken);
    }

    protected void removeXAuthToken(Header header) {
        authenticationService.removeAuthToken(header.getValue());
    }
}
