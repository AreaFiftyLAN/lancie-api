package ch.wisv.areafiftylan.products.service;

import ch.wisv.areafiftylan.ApplicationTest;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.service.repository.OrderRepository;
import ch.wisv.areafiftylan.products.service.repository.TicketOptionRepository;
import ch.wisv.areafiftylan.products.service.repository.TicketTypeRepository;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.users.model.Gender;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.service.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.spring4.SpringTemplateEngine;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationTest.class)
@ActiveProfiles("test")
@DataJpaTest
public abstract class ServiceTest {

    @MockBean
    SpringTemplateEngine springTemplateEngine;
    @MockBean
    JavaMailSender javaMailSender;

    @Autowired
    protected OrderService orderService;
    @Autowired
    protected PaymentService paymentService;
    @Autowired
    protected TicketService ticketService;
    @Autowired
    protected TestEntityManager testEntityManager;
    @Autowired
    protected OrderRepository orderRepository;
    @Autowired
    protected TicketOptionRepository ticketOptionRepository;
    @Autowired
    protected TicketTypeRepository ticketTypeRepository;
    @Autowired
    protected UserRepository userRepository;

    @Value("${a5l.orderLimit}")
    protected int ORDER_LIMIT;

    protected final String CH_MEMBER_OPTION = "chMember";
    protected final String PICKUP_SERVICE_OPTION = "pickupService";
    protected final String EXTRA_OPTION = "extraOption";
    protected final String TEST_TICKET = "test";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected User persistUser() {
        long count = userRepository.count();
        User user = new User(count + "@mail.com", new BCryptPasswordEncoder().encode("password"));
        user.getProfile()
                .setAllFields("User", String.valueOf(count), "DisplayName" + count, LocalDate.now().minusYears(19),
                        Gender.MALE, "Mekelweg" + count, "2826CD", "Delft", "0906-0666", null);

        return testEntityManager.persist(user);
    }

    protected Ticket persistTicket() {
        Ticket ticket = ticketService.requestTicketOfType(TEST_TICKET, Arrays.asList(CH_MEMBER_OPTION, PICKUP_SERVICE_OPTION));
        ticket.setValid(true);
        return testEntityManager.persist(ticket);
    }

    protected Ticket persistTicketForUser(User user) {
        Ticket ticket = ticketService.requestTicketOfType(user, TEST_TICKET, Arrays.asList(CH_MEMBER_OPTION, PICKUP_SERVICE_OPTION));
        ticket.setValid(true);
        return testEntityManager.persist(ticket);
    }

    protected Team persistTeamWithCaptain(String teamName, User captain) {
        return persistTeamWithCaptainAndMembers(teamName, captain, Collections.emptyList());
    }

    protected Team persistTeamWithCaptainAndMembers(String teamName, User captain, List<User> members) {
        Team team = new Team(teamName, captain);
        members.forEach(team::addMember);
        return testEntityManager.persist(team);
    }

    @Before
    public void setUp() {
        testEntityManager.clear();
    }

    @After
    public void tearDown() {

    }
}
