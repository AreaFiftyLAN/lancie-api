package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.service.repository.OrderRepository;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;


public class OrderRestIntegrationTest extends IntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Before
    public void initOrderTest() {
    }

    @After
    public void teamTestsCleanup() {
        logout();
        orderRepository.deleteAll();
        ticketRepository.deleteAll();
    }


    //     @RequestMapping(value = "/orders", method = RequestMethod.GET)


    //     @RequestMapping(value = "/orders", method = RequestMethod.POST)


    //     @RequestMapping(value = "/orders/{orderId}", method = RequestMethod.GET)


    //     @RequestMapping(value = "/orders/{orderId}", method = RequestMethod.POST)


}




