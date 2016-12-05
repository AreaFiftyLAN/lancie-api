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
import ch.wisv.areafiftylan.exception.TicketNotFoundException;
import ch.wisv.areafiftylan.exception.UnassignedOrderException;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.model.TicketOption;
import ch.wisv.areafiftylan.products.model.TicketType;
import ch.wisv.areafiftylan.products.model.order.Order;
import ch.wisv.areafiftylan.products.model.order.OrderStatus;
import ch.wisv.areafiftylan.products.service.repository.OrderRepository;
import ch.wisv.areafiftylan.users.model.Gender;
import ch.wisv.areafiftylan.users.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
    private PaymentService paymentSerivce;
    @Autowired
    private TestEntityManager testEntityManager;
    @Autowired
    private OrderRepository orderRepository;

    @Value("${a5l.orderLimit}")
    private int ORDER_LIMIT;

    private final String CH_MEMBER = "chMember";
    private final String PICKUP_SERVICE = "pickup";
    private final String TEST_TICKET = "test";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private User persistUser() {
        User user = new User("user@mail.com", new BCryptPasswordEncoder().encode("password"));
        user.getProfile()
                .setAllFields("first", "last", "display", LocalDate.now(), Gender.MALE, "address", "1234AB", "Delft",
                        "0612345678", null);
        return testEntityManager.persist(user);
    }

    private Ticket persistTicket() {
        TicketOption chMember = testEntityManager.persist(new TicketOption(CH_MEMBER, -5F));
        TicketOption pickupService = testEntityManager.persist(new TicketOption(PICKUP_SERVICE, 2.5F));

        TicketType ticketType =
                new TicketType(TEST_TICKET, "TestTicket", 30F, 0, LocalDateTime.now().plusDays(1L), true);
        ticketType.addPossibleOption(chMember);
        ticketType.addPossibleOption(pickupService);

        ticketType = testEntityManager.persist(ticketType);
        Ticket ticket = testEntityManager.persist(new Ticket(ticketType));
        ticket.addOption(chMember);
        ticket.addOption(pickupService);
        return testEntityManager.persist(ticket);
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
        persistTicket();
        Order order = orderService.create(TEST_TICKET, Arrays.asList(CH_MEMBER, PICKUP_SERVICE));

        assertEquals(1, order.getTickets().size());
        assertNull(order.getUser());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNullType() {
        Order order = orderService.create(null, Arrays.asList(CH_MEMBER, PICKUP_SERVICE));
        assertEquals(0, orderRepository.findAll().size());
    }

    @Test
    public void addTicketToOrderAnonymous() {
        Order order = new Order();
        Ticket ticket = persistTicket();
        order.addTicket(ticket);
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        order = orderService.addTicketToOrder(id, TEST_TICKET, null);

        assertEquals(2, order.getTickets().size());
        assertTrue(order.getTickets().stream().noneMatch(Ticket::isValid));
        assertTrue(order.getTickets().stream().allMatch(t -> t.getOwner() == null));
    }

    @Test
    public void addTicketToOrderAssigned() {
        User user = persistUser();
        Order order = new Order(user);
        Ticket ticket = persistTicket();
        ticket.setOwner(user);
        ticket = testEntityManager.persist(ticket);
        order.addTicket(ticket);
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        order = orderService.addTicketToOrder(id, TEST_TICKET, Collections.singletonList(CH_MEMBER));

        assertEquals(2, order.getTickets().size());
        assertTrue(order.getTickets().stream().noneMatch(Ticket::isValid));
        assertTrue(order.getTickets().stream().allMatch(t -> t.getOwner().equals(user)));
    }

    @Test
    public void addTicketToOrderPending() {
        thrown.expect(ImmutableOrderException.class);

        User user = persistUser();
        Ticket ticket = persistTicket();
        Order order = new Order(user);
        order.setStatus(OrderStatus.PENDING);
        order.addTicket(ticket);
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        orderService.addTicketToOrder(id, TEST_TICKET, Collections.singletonList(CH_MEMBER));
    }

    @Test
    public void addTicketToOrderPaid() {
        thrown.expect(ImmutableOrderException.class);

        User user = persistUser();
        Ticket ticket = persistTicket();
        Order order = new Order(user);
        order.setStatus(OrderStatus.PAID);
        order.addTicket(ticket);
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        orderService.addTicketToOrder(id, TEST_TICKET, Collections.singletonList(CH_MEMBER));
    }

    @Test
    public void addTicketToOrderCancelled() {
        thrown.expect(ImmutableOrderException.class);

        User user = persistUser();
        Ticket ticket = persistTicket();
        Order order = new Order(user);
        order.setStatus(OrderStatus.CANCELLED);
        order.addTicket(ticket);
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        orderService.addTicketToOrder(id, TEST_TICKET, Collections.singletonList(CH_MEMBER));
    }

    @Test
    public void addTicketToOrderExpired() {
        thrown.expect(ImmutableOrderException.class);

        User user = persistUser();
        Ticket ticket = persistTicket();
        Order order = new Order(user);
        order.setStatus(OrderStatus.EXPIRED);
        order.addTicket(ticket);
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        orderService.addTicketToOrder(id, TEST_TICKET, Collections.singletonList(CH_MEMBER));
    }

    @Test
    public void addTicketToOrderLimit() {
        User user = persistUser();
        Order order = new Order(user);
        // Fill the order
        for (int i = 0; i < ORDER_LIMIT; i++) {
            order.addTicket(persistTicket());
        }
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        try {
            orderService.addTicketToOrder(id, TEST_TICKET, Collections.singletonList(CH_MEMBER));
        } catch (IllegalStateException e) {
            assertEquals("Order numberAvailable reached", e.getMessage());
            assertEquals(ORDER_LIMIT, testEntityManager.find(Order.class, id).getTickets().size());
        }
    }

    @Test
    public void assignOrderToUser() {
        User user = persistUser();
        Order order = new Order();
        order.addTicket(persistTicket());

        Long id = testEntityManager.persistAndGetId(order, Long.class);

        orderService.assignOrderToUser(id, user.getUsername());

        assertEquals(user, testEntityManager.find(Order.class, id).getUser());
    }

    @Test
    public void assignOrderToUserWithOpenOrder() {
        User user = persistUser();
        Order order = new Order();
        Order order2 = new Order();
        order.addTicket(testEntityManager.persist(new Ticket(TicketType.TEST, true, true)));
        order2.addTicket(testEntityManager.persist(new Ticket(TicketType.TEST, true, true)));

        Long id = testEntityManager.persistAndGetId(order, Long.class);
        Long id2 = testEntityManager.persistAndGetId(order2, Long.class);

        orderService.assignOrderToUser(id, user.getUsername());
        orderService.assignOrderToUser(id2, user.getUsername());

        assertEquals(user, testEntityManager.find(Order.class, id2).getUser());
        assertNull(testEntityManager.find(Order.class, id));
    }

    @Test
    public void assignOrderToUserOrderNotFound() {
        thrown.expect(OrderNotFoundException.class);

        User user = persistUser();

        orderService.assignOrderToUser(9999L, user.getUsername());
    }

    @Test
    public void assignOrderToUserOrderIdNull() {
        thrown.expect(OrderNotFoundException.class);

        User user = persistUser();
        Order order = new Order();
        testEntityManager.persistAndGetId(order, Long.class);

        orderService.assignOrderToUser(null, user.getUsername());
    }

    @Test
    public void assignOrderToUserUsernameNotFound() {
        thrown.expect(UsernameNotFoundException.class);

        Long id = testEntityManager.persistAndGetId(new Order(), Long.class);

        orderService.assignOrderToUser(id, "nouser@mail.com");
    }

    @Test
    public void assignOrderToUserUsernameNull() {
        thrown.expect(UsernameNotFoundException.class);

        Long id = testEntityManager.persistAndGetId(new Order(), Long.class);

        orderService.assignOrderToUser(id, null);
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

        User user = persistUser();
        User user2 =
                testEntityManager.persist(new User("user2@mail.com", new BCryptPasswordEncoder().encode("password")));
        Order order = new Order(user);
        order.addTicket(persistTicket());
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        orderService.assignOrderToUser(id, user2.getUsername());

        assertEquals(user, testEntityManager.find(Order.class, id).getUser());
    }

    @Test
    public void removeTicketFromOrder() {
        Order order = new Order();
        order.addTicket(persistTicket());

        Long id = testEntityManager.persistAndGetId(order, Long.class);

        orderService.removeTicketFromOrder(id, TEST_TICKET, Arrays.asList(CH_MEMBER, PICKUP_SERVICE));

        assertEquals(0, testEntityManager.find(Order.class, id).getTickets().size());
    }

    @Test
    public void removeTicketFromOrderNoTickets() {
        thrown.expect(TicketNotFoundException.class);
        Order order = new Order();
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        orderService.removeTicketFromOrder(id, TEST_TICKET, Arrays.asList(CH_MEMBER, PICKUP_SERVICE));
    }

    @Test
    public void removeTicketFromOrderTicketNotFound() {
        Order order = new Order();
        order.addTicket(persistTicket());

        Long id = testEntityManager.persistAndGetId(order, Long.class);

        assertEquals(1, testEntityManager.find(Order.class, id).getTickets().size());

        try {
            orderService.removeTicketFromOrder(id, TEST_TICKET, Arrays.asList(CH_MEMBER, PICKUP_SERVICE));
        } catch (TicketNotFoundException e) {
            assertEquals(1, testEntityManager.find(Order.class, id).getTickets().size());
        }
    }

    @Test
    public void removeTicketFromOrderTypeNull() {
        Order order = new Order();
        order.addTicket(persistTicket());

        Long id = testEntityManager.persistAndGetId(order, Long.class);

        assertEquals(1, testEntityManager.find(Order.class, id).getTickets().size());

        try {
            orderService.removeTicketFromOrder(id, null, Arrays.asList(CH_MEMBER, PICKUP_SERVICE));
        } catch (TicketNotFoundException e) {
            assertEquals(1, testEntityManager.find(Order.class, id).getTickets().size());
        }
    }

    @Test
    public void removeTicketFromOrderTwoMatchingTickets() {
        Order order = new Order();
        order.addTicket(persistTicket());
        order.addTicket(persistTicket());

        Long id = testEntityManager.persistAndGetId(order, Long.class);

        assertEquals(2, testEntityManager.find(Order.class, id).getTickets().size());
        orderService.removeTicketFromOrder(id, TEST_TICKET, Arrays.asList(CH_MEMBER, PICKUP_SERVICE));
        assertEquals(1, testEntityManager.find(Order.class, id).getTickets().size());
    }

    @Test
    public void removeTicketFromOrderTwoMatchingTicketsDeleteTwice() {
        Order order = new Order();
        order.addTicket(persistTicket());
        order.addTicket(persistTicket());

        Long id = testEntityManager.persistAndGetId(order, Long.class);

        assertEquals(2, testEntityManager.find(Order.class, id).getTickets().size());
        orderService.removeTicketFromOrder(id, TEST_TICKET, Arrays.asList(CH_MEMBER, PICKUP_SERVICE));
        assertEquals(1, testEntityManager.find(Order.class, id).getTickets().size());
        orderService.removeTicketFromOrder(id, TEST_TICKET, Arrays.asList(CH_MEMBER, PICKUP_SERVICE));
        assertEquals(0, testEntityManager.find(Order.class, id).getTickets().size());
    }


    @Test
    public void removeTicketFromOrderOrderNotFound() {
        thrown.expect(OrderNotFoundException.class);

        Order order = new Order();
        order.addTicket(persistTicket());
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        orderService.removeTicketFromOrder(9999L, TEST_TICKET, Arrays.asList(CH_MEMBER, PICKUP_SERVICE));
    }

    @Test
    public void removeTicketFromOrderOrderIdNull() {
        thrown.expect(OrderNotFoundException.class);

        Order order = new Order();
        order.addTicket(persistTicket());
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        orderService.removeTicketFromOrder(null, TEST_TICKET, Arrays.asList(CH_MEMBER, PICKUP_SERVICE));
    }

    @Test
    public void requestPayment() {
        User user = persistUser();
        Order order = new Order(user);
        order.addTicket(persistTicket());

        Long id = testEntityManager.persistAndGetId(order, Long.class);
        String payment = orderService.requestPayment(id);
        verify(paymentSerivce, times(1)).registerOrder(Mockito.any(Order.class));
        reset(paymentSerivce);
    }

    @Test
    public void requestPaymentEmptyOrder() {
        User user = persistUser();
        Order order = new Order(user);

        Long id = testEntityManager.persistAndGetId(order, Long.class);
        try {
            String payment = orderService.requestPayment(id);
        } catch (IllegalStateException e) {
            assertEquals("Order can not be empty", e.getMessage());
            verify(paymentSerivce, never()).registerOrder(Mockito.any(Order.class));
        } finally {
            reset(paymentSerivce);
        }

    }

    @Test
    public void requestPaymentOrderUnassigned() {
        Order order = new Order();
        order.addTicket(persistTicket());

        Long id = testEntityManager.persistAndGetId(order, Long.class);
        try {
            String payment = orderService.requestPayment(id);
        } catch (UnassignedOrderException e) {
            verify(paymentSerivce, never()).registerOrder(Mockito.any(Order.class));
        } finally {
            reset(paymentSerivce);
        }
    }

    @Test
    public void requestPaymentOrderIdNull() {
        User user = persistUser();
        Order order = new Order(user);
        order.addTicket(persistTicket());

        Long id = testEntityManager.persistAndGetId(order, Long.class);
        try {
            String payment = orderService.requestPayment(null);
        } catch (OrderNotFoundException e) {
            verify(paymentSerivce, never()).registerOrder(Mockito.any(Order.class));
        } finally {
            reset(paymentSerivce);
        }
    }

    @Test
    public void updateOrderStatusByReference() {
        Order order = new Order();
        order.setUser(persistUser());
        given(paymentSerivce.updateStatus(Mockito.anyString())).willReturn(order);

        orderService.updateOrderStatusByReference("Reference");

        verify(paymentSerivce, times(1)).updateStatus(Mockito.anyString());

        reset(paymentSerivce);
    }

    @Test
    public void updateOrderStatusByReferenceOrderStatusPaid() {
        Order order = new Order();
        User user = persistUser();

        order.addTicket(persistTicket());
        order.setUser(user);
        order.setStatus(OrderStatus.PAID);
        order = testEntityManager.persist(order);
        given(paymentSerivce.updateStatus(Mockito.anyString())).willReturn(order);

        orderService.updateOrderStatusByReference("Reference");

        assertTrue(order.getTickets().stream().allMatch(Ticket::isValid));
        assertTrue(order.getTickets().stream().allMatch(t -> t.getOwner().equals(user)));

        reset(paymentSerivce);
    }

    @Test
    public void updateOrderStatusByReferenceOrderStatusAssigned() {
        Order order = new Order();
        User user = persistUser();

        order.addTicket(persistTicket());
        order.setUser(user);
        order = testEntityManager.persist(order);
        given(paymentSerivce.updateStatus(Mockito.anyString())).willReturn(order);

        orderService.updateOrderStatusByReference("Reference");

        assertTrue(order.getTickets().stream().noneMatch(Ticket::isValid));
        assertTrue(order.getTickets().stream().allMatch(t -> t.getOwner() == null));

        reset(paymentSerivce);
    }

    @Test
    public void updateOrderStatusByReferenceOrderStatusAnonymous() {
        Order order = new Order();
        thrown.expect(UnassignedOrderException.class);

        order.addTicket(persistTicket());
        order = testEntityManager.persist(order);
        given(paymentSerivce.updateStatus(Mockito.anyString())).willReturn(order);

        orderService.updateOrderStatusByReference("Reference");

        assertTrue(order.getTickets().stream().noneMatch(Ticket::isValid));
        assertTrue(order.getTickets().stream().allMatch(t -> t.getOwner() == null));

        reset(paymentSerivce);
    }

    @Test
    public void updateOrderStatusByReferenceOrderStatusPending() {
        Order order = new Order();
        User user = persistUser();

        order.addTicket(persistTicket());
        order = testEntityManager.persist(order);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        given(paymentSerivce.updateStatus(Mockito.anyString())).willReturn(order);

        orderService.updateOrderStatusByReference("Reference");

        assertTrue(order.getTickets().stream().noneMatch(Ticket::isValid));
        assertTrue(order.getTickets().stream().allMatch(t -> t.getOwner() == null));

        reset(paymentSerivce);
    }

    @Test
    public void updateOrderStatusByReferenceUnassignedOrder() {
        Order order = new Order();
        thrown.expect(UnassignedOrderException.class);

        order.addTicket(persistTicket());
        order = testEntityManager.persist(order);
        given(paymentSerivce.updateStatus(Mockito.anyString())).willReturn(order);

        orderService.updateOrderStatusByReference("Reference");

        assertTrue(order.getTickets().stream().noneMatch(Ticket::isValid));

        reset(paymentSerivce);
    }
}