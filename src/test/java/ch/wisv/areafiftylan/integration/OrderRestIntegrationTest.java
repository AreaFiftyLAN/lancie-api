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

import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.model.order.Order;
import ch.wisv.areafiftylan.products.model.order.OrderStatus;
import ch.wisv.areafiftylan.products.service.repository.OrderRepository;
import ch.wisv.areafiftylan.products.service.repository.TicketRepository;
import ch.wisv.areafiftylan.products.model.TicketType;
import ch.wisv.areafiftylan.products.service.OrderService;
import ch.wisv.areafiftylan.products.service.TicketService;
import ch.wisv.areafiftylan.users.model.User;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;


public class OrderRestIntegrationTest extends XAuthIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TicketService ticketService;

    @Value("${a5l.ticketLimit}")
    private int TICKET_LIMIT;

    private final String ORDER_ENDPOINT = "/orders/";

    private Order addOrderForUser(User user) {
        Ticket ticket = ticketRepository.save(new Ticket(
                ticketTypeRepository.findByName(TEST_TICKET).orElseThrow(IllegalArgumentException::new)));
        Order order = new Order(user);
        order.addTicket(ticket);
        return orderRepository.save(order);
    }

    private Order insertAnonOrder() {
        Order order = new Order();
        Ticket ticket = ticketRepository.save(new Ticket(
                ticketTypeRepository.findByName(TEST_TICKET).orElseThrow(IllegalArgumentException::new)));
        order.addTicket(ticket);
        return orderRepository.save(order);
    }

    @Test
    public void testGetAllOrdersAnon() {
        insertAnonOrder();

        //@formatter:off
        when().
            get(ORDER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).body("message", containsString("denied"));
        //@formatter:on
    }

    @Test
    public void testGetAllOrdersAsUser() {
        insertAnonOrder();
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(ORDER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).body("message", containsString("denied"));
        //@formatter:on
    }

    @Test
    public void testGetAllOrdersAsAdmin() {
        insertAnonOrder();
        User admin = createAdmin();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            get(ORDER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("$", hasSize(Long.valueOf(orderRepository.count()).intValue()));
        //@formatter:on
    }

    @Test
    public void testCreateAnonOrder() {
        Map<String, Object> order = new HashMap<>();
        List<String> options = Arrays.asList(CH_MEMBER, PICKUP_SERVICE);
        order.put("type", TEST_TICKET);
        order.put("options", options);

        //@formatter:off
        given().
        when().
            body(order).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_CREATED).
            body("object.user", is(nullValue())).
            body("object.status", is("ANONYMOUS")).
            body("object.tickets", hasSize(1)).
            body("object.tickets.type.name", hasItem(is(TEST_TICKET))).
            body("object.tickets.type.text", anything()).
            body("object.tickets.enabledOptions.name", hasItem(hasItems(CH_MEMBER, PICKUP_SERVICE))).
            body("object.amount",equalTo(27.5F));
        //@formatter:on
    }

    @Test
    public void testCreateOrderAsUser() {
        Map<String, Object> order = new HashMap<>();
        List<String> options = Arrays.asList(CH_MEMBER, PICKUP_SERVICE);
        order.put("type", TEST_TICKET);
        order.put("options", options);
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            body(order).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_CREATED).
            body("object.user", is(nullValue())).
            body("object.status", is("ANONYMOUS")).
            body("object.tickets", hasSize(1)).
            body("object.tickets.type.name", hasItem(is(TEST_TICKET))).
            body("object.tickets.type.text", anything()).
            body("object.tickets.enabledOptions.name", hasItem(hasItems(CH_MEMBER, PICKUP_SERVICE))).
            body("object.amount",equalTo(27.5F));
        //@formatter:on
    }

    @Test
    public void testdeleteOrderAsAdmin() {
        User admin = createAdmin();
        TicketType type = new TicketType("testEditType1", "Type for edit test", 10, 0, LocalDateTime.now().plusDays(1), true);
        type = ticketService.addTicketType(type);
        Order order = orderService.create("testEditType1", null);

        //@formatter:off
            given().
                header(getXAuthTokenHeaderForUser(admin)).
            when().
                delete(ORDER_ENDPOINT + order.getId()).
            then().
                statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testdeleteOrderAsAnon() {
        TicketType type = new TicketType("testEditType2", "Type for edit test", 10, 0, LocalDateTime.now().plusDays(1), true);
        type = ticketService.addTicketType(type);
        Order order = orderService.create("testEditType2", null);

        //@formatter:off
            when().
                delete(ORDER_ENDPOINT + order.getId()).
            then().
                statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testdeleteOrderAsUser() {
        User user = createUser();
        TicketType type = new TicketType("testEditType3", "Type for edit test", 10, 0, LocalDateTime.now().plusDays(1), true);
        type = ticketService.addTicketType(type);
        Order order = orderService.create("testEditType3", null);

        //@formatter:off
            given().
                header(getXAuthTokenHeaderForUser(user)).
            when().
                delete(ORDER_ENDPOINT + order.getId()).
            then().
                statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAddTicketToAnonOrder() {
        Order order = insertAnonOrder();
        Map<String, Object> orderDTO = new HashMap<>();
        List<String> options = Arrays.asList(CH_MEMBER, PICKUP_SERVICE);
        orderDTO.put("type", TEST_TICKET);
        orderDTO.put("options", options);

        //@formatter:off
        given().
        when().
            body(orderDTO).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT + order.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.user", is(nullValue())).
            body("object.status", is("ANONYMOUS")).
            body("object.tickets", hasSize(2)).
            body("object.tickets.type.name", hasItems(TEST_TICKET, TEST_TICKET)).
            body("object.tickets.enabledOptions.name", hasItem(hasItems(CH_MEMBER, PICKUP_SERVICE))).
            body("object.amount",equalTo(57.5F));
        //@formatter:on
    }

    @Test
    public void testAddTicketToAssignedOrderAnon() {
        User user = createUser();
        Order order = addOrderForUser(user);

        Map<String, Object> orderDTO = new HashMap<>();
        List<String> options = Arrays.asList(CH_MEMBER, PICKUP_SERVICE);
        orderDTO.put("type", TEST_TICKET);
        orderDTO.put("options", options);

        //@formatter:off
        given().
        when().
            body(orderDTO).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT + order.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAddTicketToAssignedOrderAsUser() {
        User user = createUser();
        Order order = addOrderForUser(user);

        Map<String, Object> orderDTO = new HashMap<>();
        List<String> options = Arrays.asList(CH_MEMBER, PICKUP_SERVICE);
        orderDTO.put("type", TEST_TICKET);
        orderDTO.put("options", options);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            body(orderDTO).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT + order.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.user.email", is(user.getEmail())).
            body("object.status", is("ASSIGNED")).
            body("object.tickets", hasSize(2)).
            body("object.tickets.type.name", hasItems(TEST_TICKET, TEST_TICKET)).
            body("object.tickets.enabledOptions.name", hasItem(hasItems(CH_MEMBER, PICKUP_SERVICE))).
            body("object.amount",equalTo(57.5F));
        //@formatter:on
    }

    @Test
    public void testAddTicketToAssignedOrderAsWrongUser() {
        User user = createUser();
        User user2 = createUser();
        Order order = addOrderForUser(user);

        Map<String, Object> orderDTO = new HashMap<>();
        List<String> options = Arrays.asList(CH_MEMBER, PICKUP_SERVICE);
        orderDTO.put("type", TEST_TICKET);
        orderDTO.put("options", options);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user2)).
        when().
            body(orderDTO).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT + order.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAddTicketToAssignedOrderAsAdmin() {
        User user = createUser();
        User admin = createAdmin();
        Order order = addOrderForUser(user);

        Map<String, Object> orderDTO = new HashMap<>();
        List<String> options = Arrays.asList(CH_MEMBER, PICKUP_SERVICE);
        orderDTO.put("type", TEST_TICKET);
        orderDTO.put("options", options);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            body(orderDTO).contentType(ContentType.JSON).
            post(ORDER_ENDPOINT + order.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.user.email", is(user.getEmail())).
            body("object.status", is("ASSIGNED")).
            body("object.tickets", hasSize(2)).
            body("object.tickets.type.name", hasItems(TEST_TICKET, TEST_TICKET)).
            body("object.tickets.enabledOptions.name", hasItem(hasItems(CH_MEMBER, PICKUP_SERVICE))).
            body("object.amount",equalTo(57.5F));
        //@formatter:on
    }

    @Test
    public void testRemoveTicketFromAssignedOrderAsUser() {
        User user = createUser();
        Order order = addOrderForUser(user);

        Ticket ticket = order.getTickets().iterator().next();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            delete(ORDER_ENDPOINT + order.getId() + "/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.user.email", is(user.getEmail())).
            body("object.status", is("ASSIGNED")).
            body("object.tickets", hasSize(0));
        //@formatter:on
    }

    @Test
    public void testRemoveUnrelatedTicketFromAssignedOrderAsUser() {
        User user = createUser();
        Order order = addOrderForUser(user);

        Ticket ticket = ticketRepository.save(new Ticket(
                ticketTypeRepository.findByName(TEST_TICKET).orElseThrow(IllegalArgumentException::new)));

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            delete(ORDER_ENDPOINT + order.getId() + "/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_NOT_MODIFIED);
        //@formatter:on
    }

    @Test
    public void testGetAnonOrderAsAnon() {
        Order order = insertAnonOrder();

        //@formatter:off
         when().
            get(ORDER_ENDPOINT + order.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("user", is(nullValue())).
            body("status", is("ANONYMOUS")).
            body("tickets", hasSize(1)).
            body("tickets.type.name", hasItem(is(TEST_TICKET))).
            body("tickets.type.text", anything()).
            body("tickets.enabledOptions.name", hasItem(emptyIterable())).
            body("amount",equalTo(30F));
        //@formatter:on
    }

    @Test
    public void testGetAnonOrderAsUser() {
        Order order = insertAnonOrder();
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(ORDER_ENDPOINT + order.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("user", is(nullValue())).
            body("status", is("ANONYMOUS")).
            body("tickets", hasSize(1)).
            body("tickets.type.name", hasItem(is(TEST_TICKET))).
            body("tickets.type.text", anything()).
            body("tickets.enabledOptions.name", hasItem(emptyIterable())).
            body("amount",equalTo(30F));
        //@formatter:on
    }

    @Test
    public void testGetAnonOrderAsAdmin() {
        Order order = insertAnonOrder();
        User user = createAdmin();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(ORDER_ENDPOINT + order.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("user", is(nullValue())).
            body("status", is("ANONYMOUS")).
            body("tickets", hasSize(1)).
            body("tickets.type.name", hasItem(is(TEST_TICKET))).
            body("tickets.type.text", anything()).
            body("tickets.enabledOptions", hasItem(emptyIterable())).
            body("amount",equalTo(30F));
        //@formatter:on
    }

    @Test
    public void testGetAssignedOrderAsAnon() {
        User user = createUser();
        Order order = addOrderForUser(user);

        //@formatter:off
        when().
            get(ORDER_ENDPOINT + order.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetAssignedOrderAsUser() {
        User user = createUser();
        Order order = addOrderForUser(user);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(ORDER_ENDPOINT + order.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("user.email", is(user.getEmail())).
            body("status", is("ASSIGNED")).
            body("tickets", hasSize(1)).
            body("tickets.type.name", hasItem(is(TEST_TICKET))).
            body("tickets.type.text", anything()).
            body("tickets.enabledOptions.name", hasItem(emptyIterable())).
            body("amount",equalTo(30F));
        //@formatter:on
    }

    @Test
    public void testGetAssignedOrderAsWrongUser() {
        User user = createUser();
        User user2 = createUser();
        Order order = addOrderForUser(user);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user2)).
        when().
            get(ORDER_ENDPOINT + order.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetAssignedOrderAsAdmin() {
        User user = createUser();
        User admin = createAdmin();
        Order order = addOrderForUser(user);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            get(ORDER_ENDPOINT + order.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("user.email", is(user.getEmail())).
            body("status", is("ASSIGNED")).
            body("tickets", hasSize(1)).
            body("tickets.type.name", hasItem(is(TEST_TICKET))).
            body("tickets.type.text", anything()).
            body("tickets.enabledOptions", hasItem(emptyIterable())).
            body("amount",equalTo(30F));
        //@formatter:on
    }

    @Test
    public void testAssignAnonOrderAsAnon() {
        Order order = insertAnonOrder();

        //@formatter:off
        when().
            post(ORDER_ENDPOINT + order.getId() + "/assign").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAssignAnonOrderAsUser() {
        Order order = insertAnonOrder();
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            post(ORDER_ENDPOINT + order.getId() + "/assign").
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.id", equalTo(order.getId().intValue())).
            body("object.status", is("ASSIGNED")).
            body("object.user.email", is(user.getEmail()));
        //@formatter:on
    }

    @Test
    public void testAssignAssignedOrderAsAnon() {
        User user = createUser();
        Order order = addOrderForUser(user);

        //@formatter:off
        when().
            post(ORDER_ENDPOINT + order.getId() + "/assign").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAssignAssignedOrderAsUser() {
        User user = createUser();
        Order order = addOrderForUser(user);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            post(ORDER_ENDPOINT + order.getId() + "/assign").
        then().
            statusCode(HttpStatus.SC_CONFLICT).
            body("object", is(nullValue()));
        //@formatter:on
    }

    @Test
    public void testAssignAssignedOrderAsAdmin() {
        User admin = createAdmin();
        User user = createUser();
        Order order = addOrderForUser(user);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            post(ORDER_ENDPOINT + order.getId() + "/assign").
        then().
            statusCode(HttpStatus.SC_CONFLICT).
            body("object", is(nullValue()));
        //@formatter:on
    }

    @Test
    public void testCheckoutAssignedOrderAsAnon() {
        User user = createUser();
        Order order = addOrderForUser(user);

        //@formatter:off
        when().
            post(ORDER_ENDPOINT + order.getId() + "/checkout").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("object", is(nullValue()));
        //@formatter:on
    }

    @Test
    public void testCheckoutAssignedOrderAsUser() {
        User user = createUser();
        Order order = addOrderForUser(user);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            post(ORDER_ENDPOINT + order.getId() + "/checkout").
        then().
            statusCode(HttpStatus.SC_OK).
            body("object", is("http://paymentURL.com"));
        //@formatter:on
    }

    @Test
    public void testCheckoutAssignedOrderWrongUser() {
        User user = createUser();
        User user2 = createUser();
        Order order = addOrderForUser(user);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user2)).
        when().
            post(ORDER_ENDPOINT + order.getId() + "/checkout").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("object", is(nullValue()));
        //@formatter:on
    }

    @Test
    public void testCheckoutAssignedOrderAsAdmin() {
        User user = createUser();
        User admin = createAdmin();
        Order order = addOrderForUser(user);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            post(ORDER_ENDPOINT + order.getId() + "/checkout").
        then().
            statusCode(HttpStatus.SC_OK).
            body("object", is("http://paymentURL.com"));
        //@formatter:on
    }

    @Test
    public void testCheckoutAnonOrder() {
        User user = createUser();
        Order order = addOrderForUser(user);

        //@formatter:off
        when().
            post(ORDER_ENDPOINT + order.getId() + "/checkout").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("object", is(nullValue()));
        //@formatter:on
    }

    @Test
    public void testApproveOrderAsAnon() {
        User user = createUser();
        Order order = addOrderForUser(user);

        //@formatter:off
        when().
            post(ORDER_ENDPOINT + order.getId() + "/approve").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("object", is(nullValue()));
        //@formatter:on

    }

    @Test
    public void testApproveOrderAsUser() {
        User user = createUser();
        Order order = addOrderForUser(user);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            post(ORDER_ENDPOINT + order.getId() + "/approve").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("object", is(nullValue()));
        //@formatter:on

    }

    @Test
    public void testApproveOrderAsAdmin() {
        User user = createUser();
        User admin = createAdmin();
        Order order = addOrderForUser(user);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            post(ORDER_ENDPOINT + order.getId() + "/approve").
        then().
            statusCode(HttpStatus.SC_OK).
            body("object", is(nullValue()));
        //@formatter:on
    }

    @Test
    public void testGetPaymentUrl() {
        User user = createUser();
        Order order = addOrderForUser(user);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(ORDER_ENDPOINT + order.getId() + "/url").
        then().
            statusCode(HttpStatus.SC_OK).
            header("Location", containsString("http://newpaymentURL.com"));
        //@formatter:on
    }

    @Test
    public void testGetPaymentUrlOrderAssigned() {
        User user = createUser();
        Order order = addOrderForUser(user);
        order.setStatus(OrderStatus.ASSIGNED);
        orderRepository.save(order);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(ORDER_ENDPOINT + order.getId() + "/url").
        then().
            statusCode(HttpStatus.SC_CONFLICT).
            body("message", equalTo("Operation on Order " + order.getId() + " not permitted"));
        //@formatter:on
    }

    @Test
    public void testUserAssignTicketOrder() {
        Map<String, Object> assignObject = new HashMap<>();
        User user = createUser();
        assignObject.put("userID", user.getId());
        assignObject.put("ticketType", TEST_TICKET);

        //@formatter:off
        given().
                header(getXAuthTokenHeaderForUser(user)).
                when().
                body(assignObject).contentType(ContentType.JSON).
                post("/orders/assigngiveaway").
                then().
                statusCode(HttpStatus.SC_FORBIDDEN).
                body("object", is(nullValue()));
    }

    @Test
    public void testAdminAssignTicketOrder() {
        Map<String, Object> assignObject = new HashMap<>();
        User user = createUser();
        assignObject.put("userID", user.getId());
        assignObject.put("ticketType", TEST_TICKET);
        User admin = createAdmin();

        //@formatter:off
        given().
                header(getXAuthTokenHeaderForUser(admin)).
                when().
                body(assignObject).contentType(ContentType.JSON).
                post("/orders/assigngiveaway").
                then().
                statusCode(HttpStatus.SC_CREATED).
                body("object.tickets", hasSize(1)).
                body("object.tickets.type.name", hasItem(is(TEST_TICKET))).
                body("object.tickets.type.text", anything()).
                body("object.amount",equalTo(30F));
    }
}
