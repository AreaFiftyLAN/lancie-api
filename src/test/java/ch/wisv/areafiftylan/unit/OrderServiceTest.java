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

package ch.wisv.areafiftylan.unit;

import ch.wisv.areafiftylan.exception.*;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.model.TicketType;
import ch.wisv.areafiftylan.products.model.order.Order;
import ch.wisv.areafiftylan.products.model.order.OrderStatus;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.utils.mail.MailService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class OrderServiceTest extends ServiceTest {

    @Autowired
    private MailService mailService;

    @Test
    public void getOrderById() {
        Long id = testEntityManager.persistAndGetId(new Order(), Long.class);

        Order order = orderService.getOrderById(id);

        assertEquals(id, order.getId());
    }

    @Test
    public void getOrderByIdNotFound() {
        Long id = 9999L;
        Exception exception = assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(id));
        assertEquals("Order with id: " + id + " not found", exception.getMessage());
    }

    @Test
    public void getOrderByIdNull() {
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(null));
    }

    @Test
    public void getOrderByEmptyReference() {
        assertThrows(IllegalArgumentException.class, () -> orderService.getOrderByReference(""));
    }

    @Test
    public void findOrdersByEmail() {
        User user = persistUser();

        Order order = new Order();
        order.setUser(user);
        testEntityManager.persist(order);

        Collection<Order> ordersByEmail = orderService.findOrdersByEmail(user.getEmail());

        assertEquals(1, ordersByEmail.size());
    }

    @Test
    public void findOrdersByEmailUppercase() {
        User user = persistUser();

        Order order = new Order();
        order.setUser(user);
        testEntityManager.persist(order);

        Collection<Order> ordersByEmail = orderService.findOrdersByEmail(user.getEmail().toUpperCase());

        assertEquals(1, ordersByEmail.size());
    }

    @Test
    public void findOrdersByEmailMultipleOrders() {
        User user = persistUser();

        Order order = new Order();
        Order order2 = new Order();
        order.setUser(user);
        order2.setUser(user);
        testEntityManager.persist(order);
        testEntityManager.persist(order2);

        Collection<Order> ordersByEmail = orderService.findOrdersByEmail(user.getEmail());

        assertEquals(2, ordersByEmail.size());
    }

    @Test
    public void findOrdersByEmailNull() {
        User user = persistUser();

        Order order = new Order();
        order.setUser(user);
        testEntityManager.persist(order);

        Collection<Order> ordersByEmail = orderService.findOrdersByEmail(null);

        assertEquals(0, ordersByEmail.size());
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

        List<Order> openOrders = orderService.getOpenOrders(user.getEmail());

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

        List<Order> openOrders = orderService.getOpenOrders(user.getEmail());

        assertEquals(0, openOrders.size());
    }

    @Test
    public void getOpenOrdersNoOrders() {
        User user = persistUser();

        List<Order> openOrders = orderService.getOpenOrders(user.getEmail());

        assertEquals(0, openOrders.size());
    }

    @Test
    public void create() {
        persistTicket();
        Order order = orderService.create(TEST_TICKET, Arrays.asList(CH_MEMBER_OPTION, PICKUP_SERVICE_OPTION));

        assertEquals(1, order.getTickets().size());
        assertNull(order.getUser());
    }

    @Test
    public void createNullType() {
        assertThrows(IllegalArgumentException.class, () -> orderService.create(null, Arrays.asList(CH_MEMBER_OPTION, PICKUP_SERVICE_OPTION)));
        assertEquals(0, orderRepository.findAll().size());
    }

    @Test
    public void addTicketToOrderAnonymous() {
        Order order = new Order();
        Ticket ticket = persistTicket();
        ticket.setValid(false);
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
        ticket.setValid(false);
        ticket.setOwner(user);
        ticket = testEntityManager.persist(ticket);
        order.addTicket(ticket);
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        order = orderService.addTicketToOrder(id, TEST_TICKET, Collections.singletonList(CH_MEMBER_OPTION));

        assertEquals(2, order.getTickets().size());
        assertTrue(order.getTickets().stream().noneMatch(Ticket::isValid));
        assertTrue(order.getTickets().stream().allMatch(t -> t.getOwner().equals(user)));
    }

    @Test
    public void addTicketToOrderPending() {
        User user = persistUser();
        Ticket ticket = persistTicket();
        Order order = new Order(user);
        order.setStatus(OrderStatus.PENDING);
        order.addTicket(ticket);
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        assertThrows(ImmutableOrderException.class, () -> orderService.addTicketToOrder(id, TEST_TICKET, Collections.singletonList(CH_MEMBER_OPTION)));
    }

    @Test
    public void addTicketToOrderPaid() {
        User user = persistUser();
        Ticket ticket = persistTicket();
        Order order = new Order(user);
        order.setStatus(OrderStatus.PAID);
        order.addTicket(ticket);
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        assertThrows(ImmutableOrderException.class, () -> orderService.addTicketToOrder(id, TEST_TICKET, Collections.singletonList(CH_MEMBER_OPTION)));
    }

    @Test
    public void addTicketToOrderCancelled() {
        User user = persistUser();
        Ticket ticket = persistTicket();
        Order order = new Order(user);
        order.setStatus(OrderStatus.CANCELLED);
        order.addTicket(ticket);
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        assertThrows(ImmutableOrderException.class, () -> orderService.addTicketToOrder(id, TEST_TICKET, Collections.singletonList(CH_MEMBER_OPTION)));
    }

    @Test
    public void addTicketToOrderExpired() {
        User user = persistUser();
        Ticket ticket = persistTicket();
        Order order = new Order(user);
        order.setStatus(OrderStatus.EXPIRED);
        order.addTicket(ticket);
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        assertThrows(ImmutableOrderException.class, () -> orderService.addTicketToOrder(id, TEST_TICKET, Collections.singletonList(CH_MEMBER_OPTION)));
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
            orderService.addTicketToOrder(id, TEST_TICKET, Collections.singletonList(CH_MEMBER_OPTION));
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

        orderService.assignOrderToUser(id, user.getEmail());

        assertEquals(user, testEntityManager.find(Order.class, id).getUser());
    }

    @Test
    public void assignOrderToUserOrderNotFound() {
        User user = persistUser();

        assertThrows(OrderNotFoundException.class, () -> orderService.assignOrderToUser(9999L, user.getEmail()));
    }

    @Test
    public void assignOrderToUserOrderIdNull() {
        User user = persistUser();
        Order order = new Order();
        testEntityManager.persistAndGetId(order, Long.class);

        assertThrows(OrderNotFoundException.class, () -> orderService.assignOrderToUser(null, user.getEmail()));
    }

    @Test
    public void assignOrderToUserEmailNotFound() {
        Long id = testEntityManager.persistAndGetId(new Order(), Long.class);

        assertThrows(UsernameNotFoundException.class, () -> orderService.assignOrderToUser(id, "nouser@mail.com"));
    }

    @Test
    public void assignOrderToUserEmailNull() {
        Long id = testEntityManager.persistAndGetId(new Order(), Long.class);

        assertThrows(UsernameNotFoundException.class, () -> orderService.assignOrderToUser(id, ""));
    }

    @Test
    public void assignOrderToUserPending() {
        User user = persistUser();
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        testEntityManager.persist(order);

        assertThrows(ImmutableOrderException.class, () -> orderService.assignOrderToUser(order.getId(), user.getEmail()));
    }

    @Test
    public void assignOrderToUserCancelled() {
        User user = persistUser();
        Order order = new Order();
        order.setStatus(OrderStatus.CANCELLED);
        testEntityManager.persist(order);

        assertThrows(ImmutableOrderException.class, () -> orderService.assignOrderToUser(order.getId(), user.getEmail()));
    }

    @Test
    public void assignOrderToUserPaid() {
        User user = persistUser();
        Order order = new Order();
        order.setStatus(OrderStatus.PAID);
        testEntityManager.persist(order);

        assertThrows(ImmutableOrderException.class, () -> orderService.assignOrderToUser(order.getId(), user.getEmail()));
    }

    @Test
    public void assignOrderToUserExpired() {
        User user = persistUser();
        Order order = new Order();
        order.setStatus(OrderStatus.EXPIRED);
        testEntityManager.persist(order);

        assertThrows(ImmutableOrderException.class, () -> orderService.assignOrderToUser(order.getId(), user.getEmail()));
    }

    @Test
    public void assignOrderToUserAlreadyAssigned() {
        User user = persistUser();
        User user2 =
                testEntityManager.persist(new User("user2@mail.com", new BCryptPasswordEncoder().encode("password")));
        Order order = new Order(user);
        order.addTicket(persistTicket());
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        assertThrows(ImmutableOrderException.class, () -> orderService.assignOrderToUser(id, user2.getEmail()));

        assertEquals(user, testEntityManager.find(Order.class, id).getUser());
    }

    @Test
    public void removeTicketFromOrder() {
        Order order = new Order();
        Ticket ticket = persistTicket();
        order.addTicket(ticket);

        Long id = testEntityManager.persistAndGetId(order, Long.class);

        orderService.removeTicketFromOrder(id, ticket.getId());

        assertEquals(0, testEntityManager.find(Order.class, id).getTickets().size());
    }

    @Test
    public void removeTicketFromOrderNoTickets() {
        Order order = new Order();
        Long id = testEntityManager.persistAndGetId(order, Long.class);

        assertThrows(TicketNotFoundException.class, () -> orderService.removeTicketFromOrder(id, 4L));
    }

    @Test
    public void removeTicketFromOrderTicketNotFound() {
        Order order = new Order();
        Ticket ticket = persistTicket();
        order.addTicket(ticket);

        Long id = testEntityManager.persistAndGetId(order, Long.class);

        assertEquals(1, testEntityManager.find(Order.class, id).getTickets().size());

        assertThrows(TicketNotFoundException.class, () -> orderService.removeTicketFromOrder(id, 0L));
        assertEquals(1, testEntityManager.find(Order.class, id).getTickets().size());
    }

    @Test
    public void removeTicketFromOrderTypeNull() {
        Order order = new Order();
        Ticket ticket = persistTicket();
        order.addTicket(ticket);

        Long id = testEntityManager.persistAndGetId(order, Long.class);

        assertEquals(1, testEntityManager.find(Order.class, id).getTickets().size());

        try {
            orderService.removeTicketFromOrder(id, ticket.getId() + 1);
        } catch (TicketNotFoundException e) {
            assertEquals(1, testEntityManager.find(Order.class, id).getTickets().size());
        }
    }

    @Test
    public void removeTicketFromOrderDeleteTwo() {
        Order order = new Order();
        Ticket ticket1 = persistTicket();
        Ticket ticket2 = persistTicket();
        order.addTicket(testEntityManager.persist(ticket1));
        order.addTicket(testEntityManager.persist(ticket2));

        Long id = testEntityManager.persistAndGetId(order, Long.class);

        assertEquals(2, testEntityManager.find(Order.class, id).getTickets().size());
        orderService.removeTicketFromOrder(id, ticket1.getId());
        assertEquals(1, testEntityManager.find(Order.class, id).getTickets().size());
        orderService.removeTicketFromOrder(id, ticket2.getId());
        assertEquals(0, testEntityManager.find(Order.class, id).getTickets().size());
    }


    @Test
    public void removeTicketFromOrderOrderNotFound() {
        Order order = new Order();
        Ticket ticket = persistTicket();
        order.addTicket(testEntityManager.persist(ticket));
        testEntityManager.persistAndGetId(order, Long.class);

        assertThrows(OrderNotFoundException.class, () -> orderService.removeTicketFromOrder(9999L, ticket.getId()));
    }

    @Test
    public void removeTicketFromOrderOrderIdNull() {
        Order order = new Order();
        Ticket ticket = persistTicket();
        order.addTicket(testEntityManager.persist(ticket));
        testEntityManager.persistAndGetId(order, Long.class);

        assertThrows(OrderNotFoundException.class, () -> orderService.removeTicketFromOrder(null, ticket.getId()));
    }

    @Test
    public void removeTicketFromOrderStatusPending() {
        Order order = new Order();
        Ticket ticket = persistTicket();
        order.addTicket(testEntityManager.persist(ticket));
        order.setStatus(OrderStatus.PENDING);
        testEntityManager.persistAndGetId(order, Long.class);

        assertThrows(ImmutableOrderException.class, () -> orderService.removeTicketFromOrder(order.getId(), ticket.getId()));
    }

    @Test
    public void requestPayment() {
        User user = persistUser();
        Order order = new Order(user);
        order.addTicket(persistTicket());

        Long id = testEntityManager.persistAndGetId(order, Long.class);
        orderService.requestPayment(id);
        verify(paymentService, times(1)).registerOrder(Mockito.any(Order.class));
        reset(paymentService);
    }

    @Test
    public void requestPaymentFree() {
        User user = persistUser();
        Order order = new Order(user);
        TicketType ticketType = new TicketType("testPaymentFree", "testPaymentFree", 0.0f, 15, LocalDateTime.of(3021, 6, 3, 0, 0), true);
        Ticket ticket = new Ticket(ticketType);
        assertEquals(0.0, ticket.getPrice(), 0.0001);
        order.addTicket(ticket);

        Long id = testEntityManager.persistAndGetId(order, Long.class);
        assertEquals("https://areafiftylan.nl/order-check?order=" + order.getId(), orderService.requestPayment(id));
        verify(paymentService, never()).registerOrder(Mockito.any(Order.class));
        reset(paymentService);
    }

    @Test
    public void requestPaymentEmptyOrder() {
        User user = persistUser();
        Order order = new Order(user);

        Long id = testEntityManager.persistAndGetId(order, Long.class);
        try {
            orderService.requestPayment(id);
        } catch (IllegalStateException e) {
            assertEquals("Order can not be empty", e.getMessage());
            verify(paymentService, never()).registerOrder(Mockito.any(Order.class));
        } finally {
            reset(paymentService);
        }

    }

    @Test
    public void requestPaymentOrderUnassigned() {
        Order order = new Order();
        order.addTicket(persistTicket());

        Long id = testEntityManager.persistAndGetId(order, Long.class);
        try {
            orderService.requestPayment(id);
        } catch (UnassignedOrderException e) {
            verify(paymentService, never()).registerOrder(Mockito.any(Order.class));
        } finally {
            reset(paymentService);
        }
    }

    @Test
    public void requestPaymentOrderIdNull() {
        User user = persistUser();
        Order order = new Order(user);
        order.addTicket(persistTicket());

        testEntityManager.persistAndGetId(order, Long.class);
        try {
            orderService.requestPayment(null);
        } catch (OrderNotFoundException e) {
            verify(paymentService, never()).registerOrder(Mockito.any(Order.class));
        } finally {
            reset(paymentService);
        }
    }

    @Test
    public void updateOrdersStatusById() {
        Order order = new Order();
        order.setUser(persistUser());
        order.setReference("updateOrderStatusById");
        order = testEntityManager.persist(order);
        given(paymentService.updateStatus(Mockito.anyString())).willReturn(order);

        orderService.updateOrderStatusByOrderId(order.getId());

        verify(paymentService, times(1)).updateStatus(Mockito.anyString());

        reset(paymentService);
    }

    @Test
    public void updateOrdersStatusByIdEmptyReference() {
        Order order = new Order();
        order.setUser(persistUser());
        order.setReference("");
        order = testEntityManager.persist(order);
        Order finalOrder = order;
        assertThrows(PaymentException.class, () -> orderService.updateOrderStatusByOrderId(finalOrder.getId()));
    }

    @Test
    public void updateOrderStatusByReference() {
        Order order = new Order();
        order.setUser(persistUser());
        order.setReference("updateOrderStatusByReference");
        order = testEntityManager.persist(order);
        given(paymentService.updateStatus(Mockito.anyString())).willReturn(order);

        orderService.updateOrderStatusByReference("updateOrderStatusByReference");

        verify(paymentService, times(1)).updateStatus(Mockito.anyString());

        reset(paymentService);
    }

    @Test
    public void updateOrderStatusByReferenceOrderStatusPaid() {
        Order order = new Order();
        User user = persistUser();

        order.addTicket(persistTicket());
        order.setUser(user);
        order.setStatus(OrderStatus.PAID);
        order.setReference("updateOrderStatusByReferenceOrderStatusPaid");

        order = testEntityManager.persist(order);
        given(paymentService.updateStatus(Mockito.anyString())).willReturn(order);

        orderService.updateOrderStatusByReference("updateOrderStatusByReferenceOrderStatusPaid");

        assertTrue(order.getTickets().stream().allMatch(Ticket::isValid));
        assertTrue(order.getTickets().stream().allMatch(t -> t.getOwner().equals(user)));

        reset(paymentService);
    }

    @Test
    public void updateOrderStatusByReferenceOrderStatusAssigned() {
        Order order = new Order();
        User user = persistUser();
        Ticket ticket = persistTicket();
        ticket.setValid(false);
        order.addTicket(ticket);
        order.setUser(user);
        order.setReference("updateOrderStatusByReferenceOrderStatusAssigned");
        order = testEntityManager.persist(order);
        given(paymentService.updateStatus(Mockito.anyString())).willReturn(order);

        orderService.updateOrderStatusByReference("updateOrderStatusByReferenceOrderStatusAssigned");

        assertTrue(order.getTickets().stream().noneMatch(Ticket::isValid));
        assertTrue(order.getTickets().stream().allMatch(t -> t.getOwner() == null));

        reset(paymentService);
    }

    @Test
    public void updateOrderStatusByReferenceOrderStatusAnonymous() {
        Order order = new Order();
        order.addTicket(persistTicket());
        order.setReference("updateOrderStatusByReferenceOrderStatusAnonymous");
        order = testEntityManager.persist(order);
        given(paymentService.updateStatus(Mockito.anyString())).willReturn(order);

        assertThrows(UnassignedOrderException.class, () -> orderService.updateOrderStatusByReference("updateOrderStatusByReferenceOrderStatusAnonymous"));

        assertTrue(order.getTickets().stream().noneMatch(Ticket::isValid));
        assertTrue(order.getTickets().stream().allMatch(t -> t.getOwner() == null));

        reset(paymentService);
    }

    @Test
    public void updateOrderStatusByReferenceOrderStatusPending() {
        Order order = new Order();
        User user = persistUser();

        Ticket ticket = persistTicket();
        ticket.setValid(false);
        order.addTicket(ticket);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setReference("updateOrderStatusByReferenceOrderStatusPending");
        order = testEntityManager.persist(order);
        given(paymentService.updateStatus(Mockito.anyString())).willReturn(order);

        orderService.updateOrderStatusByReference("updateOrderStatusByReferenceOrderStatusPending");

        assertTrue(order.getTickets().stream().noneMatch(Ticket::isValid));
        assertTrue(order.getTickets().stream().allMatch(t -> t.getOwner() == null));

        reset(paymentService);
    }

    @Test
    public void updateOrderStatusByReferenceUnassignedOrder() {
        Order order = new Order();

        order.addTicket(persistTicket());
        order.setReference("updateOrderStatusByReferenceUnassignedOrder");
        order = testEntityManager.persist(order);
        given(paymentService.updateStatus(Mockito.anyString())).willReturn(order);

        assertThrows(UnassignedOrderException.class, () -> orderService.updateOrderStatusByReference("updateOrderStatusByReferenceUnassignedOrder"));

        assertTrue(order.getTickets().stream().noneMatch(Ticket::isValid));

        reset(paymentService);
    }

    @Test
    public void updateOrderStatusToPaid() {
        User user = persistUser();
        Order order = new Order(user);
        order.setReference("updateOrderStatusToPaid");
        order = testEntityManager.persist(order);
        Order paidOrder = new Order(user);
        paidOrder.setStatus(OrderStatus.PAID);
        given(paymentService.updateStatus(anyString())).willReturn(paidOrder);

        orderService.updateOrderStatusByReference("updateOrderStatusToPaid");

        verify(mailService, times(1)).sendOrderConfirmation(any(Order.class));

        reset(paymentService);
    }

    @Test
    public void getPaymentURLPendingOrder() {
        User user = persistUser();
        Order order = new Order(user);
        order.setReference("getPaymentURLPendingOrder");
        order.setStatus(OrderStatus.PENDING);

        order = testEntityManager.persist(order);

        given(paymentService.getPaymentUrl(order.getReference())).willReturn("https://newpaymenturl.com");

        String paymentUrl = orderService.getPaymentUrl(order.getId());

        assertThat(paymentUrl).isEqualTo("https://newpaymenturl.com");
    }

    @Test
    public void getPaymentURLAssignedOrder() {
        User user = persistUser();
        Order order = new Order(user);
        order.setReference("getPaymentURLPendingOrder");

        order = testEntityManager.persist(order);

        Order finalOrder = order;
        assertThrows(ImmutableOrderException.class, () -> orderService.getPaymentUrl(finalOrder.getId()));
    }
}