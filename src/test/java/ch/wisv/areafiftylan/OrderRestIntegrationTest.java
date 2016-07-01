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

import ch.wisv.areafiftylan.model.Order;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.util.OrderStatus;
import ch.wisv.areafiftylan.model.util.TicketOptions;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.service.repository.OrderRepository;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import ch.wisv.areafiftylan.util.SessionData;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class OrderRestIntegrationTest extends IntegrationTest {

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Value("${a5l.ticketLimit}")
    private int TICKET_LIMIT;

    private final String ORDER_ENDPOINT = "/orders";

    private void insertTestOrders() {
        Order order1 = new Order(user);
        Order order2 = new Order(admin);

        Ticket earlyAndPickup = new Ticket(user, TicketType.EARLY_FULL, true, false);
        Ticket earlyNoPickup = new Ticket(user, TicketType.EARLY_FULL, false, false);
        Ticket regularNoPickup = new Ticket(user, TicketType.REGULAR_FULL, false, false);

        earlyAndPickup = ticketRepository.save(earlyAndPickup);
        earlyNoPickup = ticketRepository.save(earlyNoPickup);
        regularNoPickup = ticketRepository.save(regularNoPickup);

        order1.addTicket(earlyAndPickup);
        order1.addTicket(earlyNoPickup);

        order2.addTicket(regularNoPickup);

        orderRepository.save(order1);
        orderRepository.save(order2);
    }

    @After
    public void orderTestCleanup() {
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
        SessionData login = login(user.getUsername(), userCleartextPassword);

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

        SessionData login = login(admin.getUsername(), adminCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get(ORDER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("$", hasSize(2)).
            body("user.username", containsInAnyOrder(admin.getUsername(), user.getUsername())).
            body("status", hasItems("CREATING", "CREATING"));
        //formatter:on
    }

    //     @RequestMapping(value = "/orders", method = RequestMethod.POST)

    @Test
    public void testCreateSingleOrder_User() {
        Map<String, String> order = new HashMap<>();
        order.put("pickupService", "true");
        order.put("type", TicketType.TEST.toString());
        order.put("chMember", "false");
        SessionData login = login(user.getUsername(), userCleartextPassword);

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
            body("object.tickets.pickupService", hasItem(true)).
            body("object.tickets.type", hasItem(is(TicketType.TEST.toString()))).
            body("object.amount",equalTo(TicketType.TEST.getPrice() + TicketOptions.PICKUPSERVICE.getPrice()));
        //@formatter:on
    }

    @Test
    public void testCreateSingleOrderAlreadyOpen_User() {

        insertTestOrders();

        Map<String, String> order = new HashMap<>();
        order.put("pickupService", "true");
        order.put("type", TicketType.EARLY_FULL.toString());
        order.put("chMember", "false");
        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(order).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT)
        .then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testCreateSingleOrder_UserCHMember() {
        Map<String, String> order = new HashMap<>();
        order.put("pickupService", "false");
        order.put("chMember", "true");
        order.put("type", TicketType.TEST.toString());
        SessionData login = login(user.getUsername(), userCleartextPassword);

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
            body("object.tickets.type", hasItem(is(TicketType.TEST.toString()))).
            body("object.amount", equalTo(TicketType.TEST.getPrice() + TicketOptions.CHMEMBER.getPrice()));
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
        order.put("chMember", "false");
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
        order.put("chMember", "false");
        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(order).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT)
        .then().statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testCreateSingleOrderMissingTypeParameter() {
        Map<String, String> order = new HashMap<>();
        order.put("pickupService", "true");
        order.put("chMember", "false");
        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(order).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT)
        .then().statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testCreateSingleOrderMissingChParameter() {
        Map<String, String> order = new HashMap<>();
        order.put("type", TicketType.EARLY_FULL.toString());
        order.put("pickupService", "true");
        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(order).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT)
        .then().statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testCreateSingleOrderUnknownType() {
        Map<String, String> order = new HashMap<>();
        order.put("pickupService", "true");
        order.put("chMember", "false");
        order.put("type", "UNKNOWN");

        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(order).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT)
        .then().statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testCreateSingleOrderSoldOutType() {
        for (int i = 0; i < TicketType.EARLY_FULL.getLimit(); i++) {
            ticketRepository.save(new Ticket(user, TicketType.EARLY_FULL, false, false));
        }

        Map<String, String> order = new HashMap<>();
        order.put("pickupService", "true");
        order.put("type", TicketType.EARLY_FULL.toString());
        order.put("chMember", "false");
        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(order).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT)
        .then().
            statusCode(HttpStatus.SC_GONE);
        //@formatter:on
    }

    @Test
    public void testCreateSingleOrderGlobalLimit() {
        for (int i = 0; i < TICKET_LIMIT; i++) {
            ticketRepository.save(new Ticket(user, TicketType.REGULAR_FULL, false, false));
        }

        Map<String, String> order = new HashMap<>();
        order.put("pickupService", "true");
        order.put("type", TicketType.LAST_MINUTE.toString());
        order.put("chMember", "false");
        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(order).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT)
        .then().
            statusCode(HttpStatus.SC_GONE);
        //@formatter:on
    }


    //     @RequestMapping(value = "/orders/{orderId}", method = RequestMethod.GET)

    protected String createOrderAndReturnLocation() {
        Map<String, String> order = new HashMap<>();
        order.put("pickupService", "false");
        order.put("type", TicketType.TEST.toString());
        order.put("chMember", "false");
        SessionData login = login(admin.getUsername(), adminCleartextPassword);

        //@formatter:off
        return given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(order).contentType(ContentType.JSON).
            post("/users/" + user.getId() + "/orders")
        .then().extract().header("Location");
        //@formatter:on
    }

    @Test
    public void testGetOrder_Anon() {
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
        logout();

        SessionData login = login(user.getUsername(), userCleartextPassword);

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
            body("tickets", hasSize(1)).
            body("tickets.type", hasItem(is(TicketType.TEST.toString()))).
            body("tickets.pickupService", hasItem(is(false))).
            body("amount",equalTo(TicketType.TEST.getPrice()));
        //@formatter:on
    }

    @Test
    public void testGetOrderCurrentUser() {
        createOrderAndReturnLocation();
        logout();

        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/users/current/orders").
        then().
            statusCode(HttpStatus.SC_OK).
            body("[0].status", equalTo("CREATING")).
            body("[0].reference", is(nullValue())).
            body("[0].user.username", is("user")).
            body("[0].tickets", hasSize(1)).
            body("[0].tickets.type", hasItem(is(TicketType.TEST.toString()))).
            body("[0].tickets.pickupService", hasItem(is(false))).
            body("[0].amount",equalTo(TicketType.TEST.getPrice()));
        //@formatter:on
    }

    @Test
    public void testGetOpenOrderCurrentUser() {
        createOrderAndReturnLocation();
        logout();

        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/users/current/orders/open").
        then().
            statusCode(HttpStatus.SC_OK).
            body("status", hasItem(equalTo("CREATING"))).
            body("reference", hasItem(is(nullValue()))).
            body("user.username", hasItem(is("user"))).
            body("tickets", hasSize(1)).
            body("tickets.type", hasItem(hasItem(is(TicketType.TEST.toString())))).
            body("tickets.pickupService", hasItem(hasItem(is(false)))).
            body("amount",hasItem(equalTo(TicketType.TEST.getPrice())));
        //@formatter:on
    }

    @Test
    public void testGetOpenOrderCurrentUserNotFound() {
        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/users/current/orders/open").
        then().
            statusCode(HttpStatus.SC_OK).
            body("$", hasSize(0));
        //@formatter:on
    }

    @Test
    public void testGetOrder_OtherUser() {
        String location = createOrderAndReturnLocation();
        logout();

        SessionData login = login(outsider.getUsername(), outsiderCleartextPassword);

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

        SessionData login = login(admin.getUsername(), adminCleartextPassword);

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
            body("tickets.type", hasItem(is(TicketType.TEST.toString()))).
            body("tickets.pickupService", hasItem(is(false))).
            body("amount",equalTo(TicketType.TEST.getPrice()));
        //@formatter:on
    }


    //     @RequestMapping(value = "/orders/{orderId}", method = RequestMethod.POST)

    @Test
    public void testAddToOrder_Anon() {
        Map<String, String> ticket = new HashMap<>(2);
        ticket.put("pickupService", "true");
        ticket.put("type", TicketType.REGULAR_FULL.toString());
        ticket.put("chMember", "false");

        String location = createOrderAndReturnLocation();
        logout();

        //@formatter:off
        given().
            content(ticket).contentType(ContentType.JSON).
        when().
            post(location).
        then().statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on

    }

    //FIXME: DATE SENSITIVE ENUM, SHOULD BE INDEPENDENT OF DEADLINE

/*    @Test
    public void testAddToOrder_User() {
        Map<String, String> ticket = new HashMap<>(2);
        ticket.put("pickupService", "true");
        ticket.put("chMember", "false");
        ticket.put("type", TicketType.REGULAR_FULL.toString());

        String location = createOrderAndReturnLocation();
        logout();

        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(ticket).
            contentType(ContentType.JSON).
            post(location).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.tickets", hasSize(2)).
            body("object.tickets.type", hasItems(equalTo("REGULAR_FULL"), equalTo("EARLY_FULL"))).
            body("object.amount",equalTo(80.00F));

        //@formatter:on
    }*/

    @Test
    public void testAddToOrder_OtherUser() {
        Map<String, String> ticket = new HashMap<>(2);
        ticket.put("pickupService", "true");
        ticket.put("chMember", "false");
        ticket.put("type", TicketType.REGULAR_FULL.toString());

        String location = createOrderAndReturnLocation();
        logout();

        SessionData login = login(outsider.getUsername(), outsiderCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(ticket).
            contentType(ContentType.JSON).
            post(location).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on

        String locationParse = location.substring(location.lastIndexOf('/') + 1);
        Long orderId = Long.parseLong(locationParse);
        assertThat("Order still contains one ticket", orderRepository.findOne(orderId).getTickets().size(), is(1));
    }

    //FIXME: DATE SENSITIVE ENUM, SHOULD BE INDEPENDENT OF DEADLINE
/*    @Test
    public void testAddToOrder_Admin() {
        Map<String, String> ticket = new HashMap<>(2);
        ticket.put("pickupService", "true");
        ticket.put("chMember", "true");
        ticket.put("type", TicketType.REGULAR_FULL.toString());

        String location = createOrderAndReturnLocation();
        logout();

        SessionData login = login(admin.getUsername(), adminCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(ticket).
            contentType(ContentType.JSON).
            post(location).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.tickets", hasSize(2)).
            body("object.tickets.type", hasItems(equalTo("REGULAR_FULL"), equalTo("EARLY_FULL"))).
            body("object.amount",equalTo(75.00F));
        //@formatter:on
    }*/

    @Test
    public void testAddToOrderLimit_User() {

        Map<String, String> ticket = new HashMap<>(3);
        ticket.put("pickupService", "true");
        ticket.put("chMember", "false");
        ticket.put("type", TicketType.EARLY_FULL.toString());

        Order order1 = new Order(user);

        Collection<Ticket> ticketList = new ArrayList<>(5);

        ticketList.add(ticketRepository.save(new Ticket(user, TicketType.EARLY_FULL, true, false)));
        ticketList.add(ticketRepository.save(new Ticket(user, TicketType.EARLY_FULL, true, false)));
        ticketList.add(ticketRepository.save(new Ticket(user, TicketType.EARLY_FULL, true, false)));
        ticketList.add(ticketRepository.save(new Ticket(user, TicketType.EARLY_FULL, true, false)));
        ticketList.add(ticketRepository.save(new Ticket(user, TicketType.EARLY_FULL, true, false)));

        for (Ticket ticket1 : ticketList) {
            order1.addTicket(ticket1);
        }

        Order save = orderRepository.save(order1);

        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(ticket).
            contentType(ContentType.JSON).
            post("/orders/" + save.getId()).
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testAddToOrderMissingPickupParameter() {
        Map<String, String> ticket = new HashMap<>(2);
        ticket.put("chMember", "false");
        ticket.put("type", TicketType.REGULAR_FULL.toString());

        String location = createOrderAndReturnLocation();
        logout();

        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(ticket).
            contentType(ContentType.JSON).
            post(location).
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testAddToOrderMissingTypeParameter() {
        Map<String, String> ticket = new HashMap<>(2);
        ticket.put("chMember", "false");
        ticket.put("pickupService", "false");

        String location = createOrderAndReturnLocation();

        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(ticket).
            contentType(ContentType.JSON).
            post(location).
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testAddToOrderUnavailableTicket() {
        for (int i = 0; i < TicketType.EARLY_FULL.getLimit() - 1; i++) {
            ticketRepository.save(new Ticket(user, TicketType.EARLY_FULL, false, false));
        }

        Map<String, String> ticket = new HashMap<>(2);
        ticket.put("pickupService", "true");
        ticket.put("chMember", "false");
        ticket.put("type", TicketType.EARLY_FULL.toString());

        String location = createOrderAndReturnLocation();

        long countBefore = ticketRepository.count();

        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(ticket).
            contentType(ContentType.JSON).
            post(location).
        then().
            statusCode(HttpStatus.SC_GONE);
        //@formatter:on

        assertThat("Ticketcount still equal", ticketRepository.count(), equalTo(countBefore));
    }

    @Test
    public void testAddToOrderUnknownType() {
        Map<String, String> ticket = new HashMap<>(2);
        ticket.put("pickupService", "true");
        ticket.put("chMember", "false");
        ticket.put("type", "UNKNOWN");

        String location = createOrderAndReturnLocation();

        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(ticket).
            contentType(ContentType.JSON).
            post(location).
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testAddToOrderWrongStatus() {
        Map<String, String> ticket = new HashMap<>(2);
        ticket.put("pickupService", "true");
        ticket.put("chMember", "false");
        ticket.put("type", TicketType.REGULAR_FULL.toString());

        String location = createOrderAndReturnLocation();

        String locationParse = location.substring(location.lastIndexOf('/') + 1);
        Long orderId = Long.parseLong(locationParse);

        Order order = orderRepository.findOne(orderId);
        order.setStatus(OrderStatus.WAITING);
        orderRepository.save(order);

        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(ticket).
            contentType(ContentType.JSON).
            post(location).
        then().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on
    }

    @Test
    public void testRemoveTicketFromOrder() {

        Map<String, String> ticket = new HashMap<>(3);
        ticket.put("pickupService", "true");
        ticket.put("chMember", "false");
        ticket.put("type", TicketType.EARLY_FULL.toString());

        Order order1 = new Order(user);

        Ticket earlyAndPickup = new Ticket(user, TicketType.EARLY_FULL, true, false);
        Ticket earlyNoPickup = new Ticket(user, TicketType.EARLY_FULL, false, false);

        earlyAndPickup = ticketRepository.save(earlyAndPickup);
        earlyNoPickup = ticketRepository.save(earlyNoPickup);

        order1.addTicket(earlyAndPickup);
        order1.addTicket(earlyNoPickup);

        Order save = orderRepository.save(order1);

        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(ticket).contentType(ContentType.JSON).
            delete("/orders/" + save.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.tickets", hasSize(1));
        //@formatter:on
    }

    @Test
    public void testRemoveTicketFromOrderNotFound() {

        Map<String, String> ticket = new HashMap<>(3);
        ticket.put("pickupService", "true");
        ticket.put("chMember", "false");
        ticket.put("type", TicketType.REGULAR_FULL.toString());

        Order order1 = new Order(user);

        Ticket earlyAndPickup = new Ticket(user, TicketType.EARLY_FULL, true, false);
        Ticket earlyNoPickup = new Ticket(user, TicketType.EARLY_FULL, false, false);

        earlyAndPickup = ticketRepository.save(earlyAndPickup);
        earlyNoPickup = ticketRepository.save(earlyNoPickup);

        order1.addTicket(earlyAndPickup);
        order1.addTicket(earlyNoPickup);

        Order save = orderRepository.save(order1);

        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(ticket).contentType(ContentType.JSON).
            delete("/orders/" + save.getId()).
        then().
            statusCode(HttpStatus.SC_NOT_MODIFIED);
        //@formatter:on
    }

    @Test
    public void testOrderCheckout_User() {
        String location = createOrderAndReturnLocation();
        logout();

        String locationParse = location.substring(location.lastIndexOf('/') + 1);
        Long orderId = Long.parseLong(locationParse);


        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get(location + "/checkout").
        then().
            statusCode(HttpStatus.SC_OK).
            header("Location", containsString("http://paymentURL.com")).
            body("message", containsString("http://paymentURL.com"));
        //@formatter:on

        Order order = orderRepository.findOne(orderId);

        assertThat("Orderstatus updated", order.getStatus(), equalTo(OrderStatus.WAITING));
    }

    @Test
    public void testOrderCheckout_Admin() {
        String location = createOrderAndReturnLocation();
        logout();

        String locationParse = location.substring(location.lastIndexOf('/') + 1);
        Long orderId = Long.parseLong(locationParse);


        SessionData login = login(admin.getUsername(), adminCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get(location + "/checkout").
        then().
            statusCode(HttpStatus.SC_OK).
            header("Location", containsString("http://paymentURL.com")).
            body("message", containsString("http://paymentURL.com"));
        //@formatter:on

        Order order = orderRepository.findOne(orderId);

        assertThat("Orderstatus updated", order.getStatus(), equalTo(OrderStatus.WAITING));
    }

    @Test
    public void testOrderCheckout_Anon() {
        String location = createOrderAndReturnLocation();
        logout();

        String locationParse = location.substring(location.lastIndexOf('/') + 1);
        Long orderId = Long.parseLong(locationParse);

        //@formatter:off
        given().
        when().
            get(location + "/checkout").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on

        Order order = orderRepository.findOne(orderId);

        assertThat("Orderstatus updated", order.getStatus(), equalTo(OrderStatus.CREATING));
    }

    @Test
    public void testGetTicketAvailability() {
        insertTestOrders();

        //@formatter:off
        when().
            get("/tickets/available").
        then().
            body("ticketType", hasItems(equalTo("EARLY_FULL"), equalTo("REGULAR_FULL"))).
            body("ticketType", not(hasItems(equalTo(equalTo(TicketType.TEST.toString())), equalTo(TicketType.FREE
                    .toString()))));
    }

    @Test
    public void testNotAuthorizedTickets() {
        insertTestOrders();

        when().
            get("/users/current/tickets").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void testGetOneValidTicket() {
        insertTestOrders();
        SessionData login = login("user", userCleartextPassword);

        Ticket ticket = new Ticket(user, TicketType.EARLY_FULL, false, false);
        ticket.setValid(true);
        ticketRepository.saveAndFlush(ticket);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/users/current/tickets").
        then().
            body("object", hasSize(1)).
            body("[0].owner.username", is("user"));
        //@formatter:on
    }

    @Test
    public void testZeroValidTickets() {
        insertTestOrders();
        SessionData login = login("user", userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/users/current/tickets").
        then().
            body("object", hasSize(0));
        //@formatter:on
    }

    @Test
    public void testAdminCheckoutAsAdmin() {
        String location = createOrderAndReturnLocation();
        logout();

        String locationParse = location.substring(location.lastIndexOf('/') + 1);
        Long orderId = Long.parseLong(locationParse);

        SessionData login = login(admin.getUsername(), adminCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            post(location + "/approve").
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", containsString("successfully approved"));
        //@formatter:on

        Order order = orderRepository.findOne(orderId);

        assertThat("Orderstatus updated", order.getStatus(), equalTo(OrderStatus.PAID));
        for (Ticket ticket : order.getTickets()) {
            Assert.assertTrue(ticket.isValid());
        }
    }

    @Test
    public void testAdminCheckoutAsUser() {
        String location = createOrderAndReturnLocation();
        logout();

        String locationParse = location.substring(location.lastIndexOf('/') + 1);
        Long orderId = Long.parseLong(locationParse);

        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            post(location + "/approve").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on

        Order order = orderRepository.findOne(orderId);

        assertThat("Orderstatus WAITING", order.getStatus(), equalTo(OrderStatus.CREATING));
        for (Ticket ticket : order.getTickets()) {
            Assert.assertFalse(ticket.isValid());
        }
    }
}




