package ch.wisv.areafiftylan.unit;

import ch.wisv.areafiftylan.TestRunner;
import ch.wisv.areafiftylan.extras.mailupdates.service.SubscriptionServiceImpl;
import ch.wisv.areafiftylan.extras.rfid.service.RFIDServiceImpl;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.service.*;
import ch.wisv.areafiftylan.products.service.repository.OrderRepository;
import ch.wisv.areafiftylan.products.service.repository.TicketOptionRepository;
import ch.wisv.areafiftylan.products.service.repository.TicketRepository;
import ch.wisv.areafiftylan.products.service.repository.TicketTypeRepository;
import ch.wisv.areafiftylan.seats.service.SeatServiceImpl;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.teams.service.TeamServiceImpl;
import ch.wisv.areafiftylan.users.model.Gender;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.service.UserRepository;
import ch.wisv.areafiftylan.users.service.UserServiceImpl;
import ch.wisv.areafiftylan.utils.mail.MailServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
@Import({TestRunner.class, OrderServiceImpl.class, RFIDServiceImpl.class, SeatServiceImpl.class, TicketServiceImpl.class, SubscriptionServiceImpl.class, UserServiceImpl.class, TeamServiceImpl.class})
public abstract class ServiceTest {

    @MockBean
    SpringTemplateEngine springTemplateEngine;
    @MockBean
    MailServiceImpl mailService;
    @MockBean
    MolliePaymentService paymentService;

    @Autowired
    protected OrderService orderService;
    @Autowired
    protected TicketService ticketService;
    /**
     * The usage of testEntityManager comes with the @DataJpaTest, which complicates things a lot. Don't use it for new tests
     */
    @Autowired
    @Deprecated
    protected TestEntityManager testEntityManager;
    @Autowired
    protected OrderRepository orderRepository;
    @Autowired
    protected TicketOptionRepository ticketOptionRepository;
    @Autowired
    protected TicketTypeRepository ticketTypeRepository;
    @Autowired
    protected TicketRepository ticketRepository;
    @Autowired
    protected UserRepository userRepository;

    @Value("${a5l.orderLimit}")
    protected int ORDER_LIMIT;

    protected final String CH_MEMBER_OPTION = "chMember";
    protected final String PICKUP_SERVICE_OPTION = "pickupService";
    protected final String EXTRA_OPTION = "extraOption";
    protected final String TEST_TICKET = "test";

    protected User persistUser() {
        long count = userRepository.count();
        User user = new User(count + "@mail.com", new BCryptPasswordEncoder().encode("password"));
        user.getProfile()
                .setAllFields("User", String.valueOf(count), "DisplayName" + count, LocalDate.now().minusYears(19),
                        Gender.MALE, "Mekelweg" + count, "2826CD", "Delft", "0906-0666", null);

        return testEntityManager.persist(user);
    }

    protected Ticket persistTicket() {
        Ticket ticket =
                ticketService.requestTicketOfType(TEST_TICKET, Arrays.asList(CH_MEMBER_OPTION, PICKUP_SERVICE_OPTION));
        ticket.setValid(false);
        return testEntityManager.persist(ticket);
    }

    protected Ticket persistTicketForUser(User user) {
        Ticket ticket = ticketService
                .requestTicketOfType(user, TEST_TICKET, Arrays.asList(CH_MEMBER_OPTION, PICKUP_SERVICE_OPTION));
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

    @BeforeEach
    public void setUp() {
        testEntityManager.clear();
    }

    @AfterEach
    public void tearDown() {

    }
}
