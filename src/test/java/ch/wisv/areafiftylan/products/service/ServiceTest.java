package ch.wisv.areafiftylan.products.service;

import ch.wisv.areafiftylan.ApplicationTest;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.service.repository.OrderRepository;
import ch.wisv.areafiftylan.products.service.repository.TicketOptionRepository;
import ch.wisv.areafiftylan.products.service.repository.TicketTypeRepository;
import ch.wisv.areafiftylan.users.model.Gender;
import ch.wisv.areafiftylan.users.model.User;
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

    @Value("${a5l.orderLimit}")
    protected int ORDER_LIMIT;

    protected final String CH_MEMBER_OPTION = "chMember";
    protected final String PICKUP_SERVICE_OPTION = "pickupService";
    protected final String EXTRA_OPTION = "extraOption";
    protected final String TEST_TICKET = "test";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected User persistUser() {
        User user = new User("user@mail.com", new BCryptPasswordEncoder().encode("password"));
        user.getProfile()
                .setAllFields("first", "last", "display", LocalDate.now(), Gender.MALE, "address", "1234AB", "Delft",
                        "0612345678", null);
        return testEntityManager.persist(user);
    }

    protected Ticket persistTicket() {
        Ticket ticket = ticketService.requestTicketOfType(TEST_TICKET, Arrays.asList(CH_MEMBER_OPTION, PICKUP_SERVICE_OPTION));
        return testEntityManager.persist(ticket);
    }

    @Before
    public void setUp() {
        testEntityManager.clear();
    }

    @After
    public void tearDown() {

    }
}
