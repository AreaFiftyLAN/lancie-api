package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.Order;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.service.repository.OrderRepository;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import ch.wisv.areafiftylan.util.SessionData;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
    public void initOrderTest() {    }

    private void insertTestOrders() {
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
        insertTestOrders();

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
    public void testCreateSingleOrder_User() {
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
            post(ORDER_ENDPOINT)
        .then().
            statusCode(HttpStatus.SC_CREATED).
            body("object.user.username", is("user")).
            body("object.status", is("CREATING")).
            body("object.tickets", hasSize(1)).
            body("object.tickets.pickupService", hasItem(false)).
            body("object.tickets.type", hasItem(is("EARLY_FULL")));
        //@formatter:on
    }

    @Test
    public void testCreateSingleOrder_Anon() {
        Map<String, String> order = new HashMap<>();
        order.put("pickupService", "false");
        order.put("type", TicketType.EARLY_FULL.toString());

        //@formatter:off
        given().
        when().
            content(order).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", containsString("CSRF"));
        //@formatter:on
    }

    @Test
    public void testCreateSingleOrder_AnonWithCSRF() {
        Map<String, String> order = new HashMap<>();
        order.put("pickupService", "false");
        order.put("type", TicketType.EARLY_FULL.toString());

        //@formatter:off
        Response tokenResponse =
            given().
                filter(sessionFilter).
            when().
                get("/token").
            then().
                extract().response();

        given().
            filter(sessionFilter).
            header("X-CSRF-TOKEN", tokenResponse.getHeader("X-CSRF-TOKEN")).
        when().
            content(order).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", containsString("denied"));
        //@formatter:on
    }

    @Test
    public void testCreateSingleOrderMissingPickupParameter() {
        Map<String, String> order = new HashMap<>();
        order.put("type", TicketType.EARLY_FULL.toString());
        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(order).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT)
        .then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testCreateSingleOrderMissingTypeParameter(){
        Map<String, String> order = new HashMap<>();
        order.put("pickupService", "true");
        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(order).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT)
        .then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }


    //     @RequestMapping(value = "/orders/{orderId}", method = RequestMethod.GET)

    private String createOrderAndReturnLocation(){
        Map<String, String> order = new HashMap<>();
        order.put("pickupService", "false");
        order.put("type", TicketType.EARLY_FULL.toString());
        SessionData login = login("user");

        //@formatter:off

        return given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(order).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT)
        .then().extract().header("Location");
    }

    @Test
    public void testGetOrder_Anon(){
        String location = createOrderAndReturnLocation();

        logout();

        //@formatter:off
        when().
            get(location).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", containsString("denied"));
        //@formatter:on

    }

    @Test
    public void testGetOrder_User() {

        String location = createOrderAndReturnLocation();

        //@formatter:off
        Response tokenResponse =
            given().
                filter(sessionFilter).
            when().
                get("/token").
            then().
                extract().response();

        given().
            filter(sessionFilter).
            header("X-CSRF-TOKEN", tokenResponse.getHeader("X-CSRF-TOKEN")).
        when().
            get(location).
        then().
            statusCode(HttpStatus.SC_OK).
            body("status", is("CREATING")).
            body("reference", is(nullValue())).
            body("user.username", is("user")).
            body("tickets.type", hasItem(is("EARLY_FULL"))).
            body("tickets.pickupService", hasItem(is(false)));
        //@formatter:on
    }

    @Test
    public void testGetOrder_OtherUser() {
        User otherUser = new User("otherUser", new BCryptPasswordEncoder().encode("password"), "otheruser@mail.com");
        otherUser = userRepository.save(otherUser);

        String location = createOrderAndReturnLocation();

        logout();

        SessionData login = login("otheruser");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get(location).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("object", is(nullValue()));
        //@formatter:on

    }

    @Test
    public void testGetOrder_Admin() {
        String location = createOrderAndReturnLocation();

        logout();

        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get(location).
        then().
            statusCode(HttpStatus.SC_OK).
            body("status", is("CREATING")).
            body("reference", is(nullValue())).
            body("user.username", is("user")).
            body("tickets.type", hasItem(is("EARLY_FULL"))).
            body("tickets.pickupService", hasItem(is(false)));;
        //@formatter:on
    }


    //     @RequestMapping(value = "/orders/{orderId}", method = RequestMethod.POST)


}




