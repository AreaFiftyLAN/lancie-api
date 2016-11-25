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

package ch.wisv.areafiftylan.products.service;

import ch.wisv.areafiftylan.ApplicationTest;
import ch.wisv.areafiftylan.exception.ImmutableOrderException;
import ch.wisv.areafiftylan.exception.OrderNotFoundException;
import ch.wisv.areafiftylan.products.model.Order;
import ch.wisv.areafiftylan.products.model.OrderStatus;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.model.TicketType;
import ch.wisv.areafiftylan.users.model.Gender;
import ch.wisv.areafiftylan.users.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.spring4.SpringTemplateEngine;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationTest.class)
@ActiveProfiles("test")
@DataJpaTest
public class OrderServiceTest {

    @MockBean
    SpringTemplateEngine springTemplateEngine;
    @MockBean
    JavaMailSender javaMailSender;

    @Autowired
    private OrderService orderService;
    @Autowired
    private TestEntityManager testEntityManager;
    @Autowired
    private OrderRepository orderRepository;

    @Value("${a5l.orderLimit}")
    private int ORDER_LIMIT;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private User persistUser() {
        User user = new User("user@mail.com", new BCryptPasswordEncoder().encode("password"));
        user.getProfile()
                .setAllFields("first", "last", "display", LocalDate.now(), Gender.MALE, "address", "1234AB", "Delft",
                        "0612345678", null);
        return testEntityManager.persist(user);
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void getOrderById() {
        Long id = testEntityManager.persistAndGetId(new Order(), Long.class);

        Order order = orderService.getOrderById(id);

        assertEquals(id, order.getId());
    }

    @Test
    public void getOrderByIdNotFound() {
        Long id = 9999L;
        thrown.expect(OrderNotFoundException.class);
        thrown.expectMessage("Order with id: " + id + " not found");

        orderService.getOrderById(id);
    }

    @Test
    public void getOrderByIdNull() {
        thrown.expect(OrderNotFoundException.class);
        thrown.expectMessage("Order with id: ");

        orderService.getOrderById(null);
    }

    @Test
    public void findOrdersByUsername() {
        User user = persistUser();

        Order order = new Order();
        order.setUser(user);
        order = testEntityManager.persist(order);

        Collection<Order> ordersByUsername = orderService.findOrdersByUsername(user.getUsername());

        assertEquals(1, ordersByUsername.size());
    }

    @Test
    public void findOrdersByUsernameUppercase() {
        User user = persistUser();

        Order order = new Order();
        order.setUser(user);
        order = testEntityManager.persist(order);

        Collection<Order> ordersByUsername = orderService.findOrdersByUsername(user.getUsername().toUpperCase());

        assertEquals(1, ordersByUsername.size());

    }

    @Test
    public void findOrdersByUsernameMultipleOrders() {
        User user = persistUser();

        Order order = new Order();
        Order order2 = new Order();
        order.setUser(user);
        order2.setUser(user);
        testEntityManager.persist(order);
        testEntityManager.persist(order2);

        Collection<Order> ordersByUsername = orderService.findOrdersByUsername(user.getUsername());

        assertEquals(2, ordersByUsername.size());
    }

    @Test
    public void findOrdersByUsernameNull() {
        User user = persistUser();

        Order order = new Order();
        order.setUser(user);
        testEntityManager.persist(order);

        Collection<Order> ordersByUsername = orderService.findOrdersByUsername(null);

        assertEquals(0, ordersByUsername.size());
    }

    @Test
    public void getOpenOrdersOneOpen() {
        User user = persistUser();

        Order order = new Order();
        order.setUser(user);
        testEntityManager.persist(order);
        Order order2 = new Order();
        order2.setUser(user);
        order2.setStatus(OrderStatus.PAID);
        testEntityManager.persist(order2);

        List<Order> openOrders = orderService.getOpenOrders(user.getUsername());

        assertEquals(1, openOrders.size());
    }

    @Test
    public void getOpenOrdersZeroOpen() {
        User user = persistUser();

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PAID);
        testEntityManager.persist(order);
        Order order2 = new Order();
        order2.setUser(user);
        order2.setStatus(OrderStatus.CANCELLED);
        testEntityManager.persist(order2);

        List<Order> openOrders = orderService.getOpenOrders(user.getUsername());

        assertEquals(0, openOrders.size());
    }

    @Test
    public void getOpenOrdersNoOrders() {
        User user = persistUser();

        List<Order> openOrders = orderService.getOpenOrders(user.getUsername());

        assertEquals(0, openOrders.size());
    }

    @Test
    public void create() {
        Order order = orderService.create(TicketType.TEST, true, true);

        assertEquals(1, order.getTickets().size());
        assertNull(order.getUser());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNullType() {
        orderService.create(null, true, true);
        assertEquals(0, orderRepository.findAll().size());
    }

    @Test
    public void addTicketToOrderAnonymous() {
        Order order = new Order();
        Ticket ticket = testEntityManager.persist(new Ticket(TicketType.TEST, true, true));
        order.addTicket(ticket);
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        order = orderService.addTicketToOrder(id, TicketType.TEST, false, false);

        assertEquals(2, order.getTickets().size());
        assertTrue(order.getTickets().stream().noneMatch(Ticket::isValid));
        assertTrue(order.getTickets().stream().allMatch(t -> t.getOwner() == null));
    }

    @Test
    public void addTicketToOrderAssigned() {
        User user = persistUser();
        Order order = new Order(user);
        Ticket ticket = testEntityManager.persist(new Ticket(user, TicketType.TEST, true, true));
        order.addTicket(ticket);
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        order = orderService.addTicketToOrder(id, TicketType.TEST, false, false);

        assertEquals(2, order.getTickets().size());
        assertTrue(order.getTickets().stream().noneMatch(Ticket::isValid));
        assertTrue(order.getTickets().stream().allMatch(t -> t.getOwner().equals(user)));
    }

    @Test
    public void addTicketToOrderPending() {
        thrown.expect(ImmutableOrderException.class);

        User user = persistUser();
        Ticket ticket = testEntityManager.persist(new Ticket(TicketType.TEST, true, true));
        Order order = new Order(user);
        order.setStatus(OrderStatus.PENDING);
        order.addTicket(ticket);
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        orderService.addTicketToOrder(id, TicketType.TEST, false, false);
    }

    @Test
    public void addTicketToOrderPaid() {
        thrown.expect(ImmutableOrderException.class);

        User user = persistUser();
        Ticket ticket = testEntityManager.persist(new Ticket(TicketType.TEST, true, true));
        Order order = new Order(user);
        order.setStatus(OrderStatus.PAID);
        order.addTicket(ticket);
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        orderService.addTicketToOrder(id, TicketType.TEST, false, false);
    }

    @Test
    public void addTicketToOrderCancelled() {
        thrown.expect(ImmutableOrderException.class);

        User user = persistUser();
        Ticket ticket = testEntityManager.persist(new Ticket(TicketType.TEST, true, true));
        Order order = new Order(user);
        order.setStatus(OrderStatus.CANCELLED);
        order.addTicket(ticket);
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        orderService.addTicketToOrder(id, TicketType.TEST, false, false);
    }

    @Test
    public void addTicketToOrderExpired() {
        thrown.expect(ImmutableOrderException.class);

        User user = persistUser();
        Ticket ticket = testEntityManager.persist(new Ticket(TicketType.TEST, true, true));
        Order order = new Order(user);
        order.setStatus(OrderStatus.EXPIRED);
        order.addTicket(ticket);
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        orderService.addTicketToOrder(id, TicketType.TEST, false, false);
    }

    @Test
    public void addTicketToOrderLimit() {
        User user = persistUser();
        Order order = new Order(user);
        // Fill the order
        for (int i = 0; i < ORDER_LIMIT; i++) {
            order.addTicket(testEntityManager.persist(new Ticket(TicketType.TEST, true, true)));
        }
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        try {
            orderService.addTicketToOrder(id, TicketType.TEST, false, false);
        } catch (IllegalStateException e) {
            assertEquals("Order limit reached", e.getMessage());
            assertEquals(ORDER_LIMIT, testEntityManager.find(Order.class, id).getTickets().size());
        }
    }

    @Test
    public void assignOrderToUser() {
        User user = persistUser();
        Order order = new Order();
        order.addTicket(testEntityManager.persist(new Ticket(TicketType.TEST, true, true)));

        Long id = testEntityManager.persistAndGetId(order, Long.class);

        orderService.assignOrderToUser(id, user.getUsername());

        assertEquals(user, testEntityManager.find(Order.class, id).getUser());

    }

    @Test
    public void assignOrderToUserOrderNotFound() {
        thrown.expect(OrderNotFoundException.class);

        User user = persistUser();

        orderService.assignOrderToUser(9999L, user.getUsername());
    }

    @Test
    public void assignOrderToUserUsernameNotFound() {
        thrown.expect(UsernameNotFoundException.class);

        Long id = testEntityManager.persistAndGetId(new Order(), Long.class);

        orderService.assignOrderToUser(id, "nouser@mail.com");
    }

    @Test
    public void assignOrderToUserPending() {
        thrown.expect(ImmutableOrderException.class);

        User user = persistUser();
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        testEntityManager.persist(order);

        orderService.assignOrderToUser(order.getId(), user.getUsername());
    }

    @Test
    public void assignOrderToUserCancelled() {
        thrown.expect(ImmutableOrderException.class);

        User user = persistUser();
        Order order = new Order();
        order.setStatus(OrderStatus.CANCELLED);
        testEntityManager.persist(order);

        orderService.assignOrderToUser(order.getId(), user.getUsername());
    }

    @Test
    public void assignOrderToUserPaid() {
        thrown.expect(ImmutableOrderException.class);

        User user = persistUser();
        Order order = new Order();
        order.setStatus(OrderStatus.PAID);
        testEntityManager.persist(order);

        orderService.assignOrderToUser(order.getId(), user.getUsername());
    }

    @Test
    public void assignOrderToUserExpired() {
        thrown.expect(ImmutableOrderException.class);

        User user = persistUser();
        Order order = new Order();
        order.setStatus(OrderStatus.EXPIRED);
        testEntityManager.persist(order);

        orderService.assignOrderToUser(order.getId(), user.getUsername());
    }

    @Test
    public void assignOrderToUserAlreadyAssigned() {
        thrown.expect(ImmutableOrderException.class);
        thrown.expectMessage("Order already assigned!");

        User user = persistUser();
        User user2 =
                testEntityManager.persist(new User("user2@mail.com", new BCryptPasswordEncoder().encode("password")));
        Order order = new Order(user);
        order.addTicket(testEntityManager.persist(new Ticket(TicketType.TEST, true, true)));
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        orderService.assignOrderToUser(id, user2.getUsername());

        assertEquals(user, testEntityManager.find(Order.class, id).getUser());
    }

    //    @Test
    //    public void removeTicketFromOrder() {
    //
    //    }
    //
    //    @Test
    //    public void requestPayment() {
    //
    //    }
    //
    //    @Test
    //    public void updateOrderStatus() {
    //
    //    }
    //
    //    @Test
    //    public void updateOrderStatus1() {
    //
    //    }
    //
    //    @Test
    //    public void adminApproveOrder() {
    //
    //    }
    //
    //    @Test
    //    public void expireOrder() {
    //
    //    }
    //
    //    @Test
    //    public void getAvailableTickets() {
    //
    //    }

}