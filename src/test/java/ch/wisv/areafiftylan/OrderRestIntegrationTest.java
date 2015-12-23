package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.Order;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.service.repository.OrderRepository;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import ch.wisv.areafiftylan.util.SessionData;
import com.jayway.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;


public class OrderRestIntegrationTest extends IntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TicketRepository ticketRepository;

    private final String ORDER_ENDPOINT = "/orders";

    @Before
    public void initOrderTest() {
        Order order1 = new Order(user);
        Order order2 = new Order(admin);

        Ticket earlyAndPickup = new Ticket(user, TicketType.EARLY_FULL, true);
        Ticket earlyNoPickup = new Ticket(user, TicketType.EARLY_FULL, false);
        Ticket regularNoPickup = new Ticket(user, TicketType.REGULAR_FULL, false);
        Ticket lateAndPickup = new Ticket(user, TicketType.LATE_FULL, true);

        earlyAndPickup = ticketRepository.save(earlyAndPickup);
        earlyNoPickup = ticketRepository.save(earlyNoPickup);
        regularNoPickup = ticketRepository.save(regularNoPickup);
        lateAndPickup = ticketRepository.save(lateAndPickup);

        order1.addTicket(earlyAndPickup);
        order1.addTicket(earlyNoPickup);

        order2.addTicket(regularNoPickup);
        order2.addTicket(lateAndPickup);

        order1 = orderRepository.save(order1);
        order2 = orderRepository.save(order2);
    }

    @After
    public void orderTestCleanup() {
        logout();
        orderRepository.findAll().forEach((order) -> {
            order.clearTickets();
            orderRepository.save(order);
        });
        orderRepository.deleteAll();
        ticketRepository.deleteAll();
    }


    //     @RequestMapping(value = "/orders", method = RequestMethod.GET)
    @Test
    public void testViewAllOrders_Anon() {
        when().get(ORDER_ENDPOINT).
                then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void testViewAllOrders_User() {
        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get(ORDER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).body("message", containsString("denied"));
        //@formatter:on

    }

    @Test
    public void testViewAllOrders_Admin() {
        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get(ORDER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("$", hasSize(2)).
            body("user.username", containsInAnyOrder("admin", "user")).
            body("status", hasItems("CREATING", "CREATING"));
        //formatter:on

    }


    //     @RequestMapping(value = "/orders", method = RequestMethod.POST)

    @Test
    public void testCreateSingleOrder_User(){
        Map<String, String> order = new HashMap<>();
        order.put("pickupService", "false");
        order.put("type", TicketType.EARLY_FULL.toString());
        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(order).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_CREATED).
            body("object.user.username", is("user")).
            body("object.status", is("CREATING")).
            body("object.tickets", hasSize(1)).
            body("object.tickets.pickupService", hasItem(false));
        //@formatter:on
    }


    //     @RequestMapping(value = "/orders/{orderId}", method = RequestMethod.GET)


    //     @RequestMapping(value = "/orders/{orderId}", method = RequestMethod.POST)


}




