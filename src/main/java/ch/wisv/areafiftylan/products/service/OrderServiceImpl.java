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

import ch.wisv.areafiftylan.exception.*;
import ch.wisv.areafiftylan.products.model.*;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.service.UserService;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ExpiredOrderRepository expiredOrderRepository;
    private final TicketService ticketService;
    private final UserService userService;
    private final PaymentService paymentService;

    @Value("${a5l.orderLimit}")
    private int ORDER_LIMIT;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, UserService userService, TicketService ticketService,
                            PaymentService paymentService, ExpiredOrderRepository expiredOrderRepository) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.ticketService = ticketService;
        this.paymentService = paymentService;
        this.expiredOrderRepository = expiredOrderRepository;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order with id: " + id + " not found"));
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Collection<Order> findOrdersByUsername(String username) {
        return orderRepository.findAllByUserUsernameIgnoreCase(username);
    }

    @Override
    public List<Order> getOpenOrders(String username) {
        Collection<Order> ordersByUsername = findOrdersByUsername(username);

        return ordersByUsername.stream().
                filter(o -> o.getStatus().equals(OrderStatus.ASSIGNED)).
                collect(Collectors.toList());
    }

    @Override
    public Order create(TicketType type, boolean pickupService, boolean chMember) {

        if (type == null) {
            throw new IllegalArgumentException("TicketType can't be null!");
        }

        // Request a ticket to see if one is available. If a ticket is sold out, the method ends here due to the
        // exception thrown. Else, we'll get a new ticket to add to the order.
        Ticket ticket = ticketService.requestTicketOfType(type, pickupService, chMember);

        Order order = new Order();

        order.addTicket(ticket);

        return orderRepository.save(order);
    }

    @Override
    public Order addTicketToOrder(Long orderId, TicketType type, boolean pickupService, boolean chMember) {
        Order order = getOrderById(orderId);

        // Check Order status
        if (!(order.getStatus().equals(OrderStatus.ANONYMOUS) || order.getStatus().equals(OrderStatus.ASSIGNED))) {
            throw new ImmutableOrderException(orderId);
        }

        // Check amount of Tickets already in Order
        if (order.getTickets().size() >= ORDER_LIMIT) {
            throw new IllegalStateException("Order limit reached");
        }

        // Request a ticket to see if one is available. If a ticket is sold out, the method ends here due to the
        // exception thrown. Else, we'll get a new ticket to add to the order.
        Ticket ticket;
        if (order.getUser() != null) {
            ticket = ticketService.requestTicketOfType(order.getUser(), type, pickupService, chMember);
        } else {
            ticket = ticketService.requestTicketOfType(type, pickupService, chMember);
        }
        order.addTicket(ticket);
        return orderRepository.save(order);
    }

    @Override
    public Order assignOrderToUser(Long orderId, String username) {
        Order order = getOrderById(orderId);
        User user = userService.getUserByUsername(username);

        if (order.getStatus() != OrderStatus.ANONYMOUS) {
            throw new ImmutableOrderException("Order already assigned!");
        }

        order.setUser(user);
        order.setStatus(OrderStatus.ASSIGNED);
        return orderRepository.save(order);
    }

    @Override
    public Order removeTicketFromOrder(Long orderId, TicketType type, boolean pickupService, boolean chMember) {
        Order order = getOrderById(orderId);
        if (order.getStatus().equals(OrderStatus.ANONYMOUS) || order.getStatus().equals(OrderStatus.ASSIGNED)) {

            // Find a Ticket in the order, equal to the given DTO. Throw an exception when the ticket doesn't exist
            Ticket ticket = order.getTickets().
                    stream().
                    filter(isEqualToInput(type, pickupService, chMember)).
                    findFirst().orElseThrow(TicketNotFoundException::new);

            order.getTickets().remove(ticket);
            ticketService.removeTicket(ticket.getId());

            return orderRepository.save(order);

        } else {
            throw new ImmutableOrderException(orderId);
        }
    }

    private static Predicate<Ticket> isEqualToInput(TicketType type, boolean pickupService, boolean chMember) {
        return t -> (t.getType() == type) && (t.isChMember() == chMember) && (t.hasPickupService() == pickupService);
    }

    @Override
    public String requestPayment(Long orderId) {
        Order order = getOrderById(orderId);
        if (order.getAmount() == 0) {
            throw new IllegalStateException("Order can not be empty");
        }
        if (order.getStatus() != OrderStatus.ASSIGNED) {
            throw new UnassignedOrderException(order.getId());
        }

        return paymentService.registerOrder(order);
    }

    @Override
    public Order updateOrderStatusByReference(String orderReference) {

        Order order = paymentService.updateStatus(orderReference);

        // Set all tickets from this Order to valid
        validateTicketsIfPaid(order);
        return order;
    }

    private void validateTicketsIfPaid(Order order) {
        if (order.getUser() == null) {
            throw new UnassignedOrderException(order.getId());
        }

        if (order.getStatus().equals(OrderStatus.PAID)) {
            for (Ticket ticket : order.getTickets()) {
                ticketService.assignTicketToUser(ticket.getId(), order.getUser().getUsername());
                ticketService.validateTicket(ticket.getId());
            }
        }
    }

    @Override
    public Order updateOrderStatusByOrderId(Long orderId) {
        Order order = getOrderById(orderId);
        if (!Strings.isNullOrEmpty(order.getReference())) {
            return paymentService.updateStatus(order.getReference());
        } else {
            throw new PaymentException("Order with id " + order + " has not been checked out yet");
        }
    }

    @Override
    public void adminApproveOrder(Long orderId) {
        Order order = getOrderById(orderId);
        if (order.getUser() != null) {
            order.setStatus(OrderStatus.PAID);
            validateTicketsIfPaid(order);
            orderRepository.save(order);
        } else {
            throw new UnassignedOrderException(orderId);
        }
    }

    @Override
    public void expireOrder(Order o) {
        orderRepository.delete(o);
        ExpiredOrder eo = new ExpiredOrder(o);
        expiredOrderRepository.save(eo);
        o.getTickets().forEach(t -> ticketService.removeTicket(t.getId()));
    }

    @Override
    public Collection<TicketInformationResponse> getAvailableTickets() {
        Collection<TicketInformationResponse> ticketInfo = new ArrayList<>();

        for (TicketType ticketType : TicketType.values()) {
            if (ticketType.isBuyable() && ticketType != TicketType.TEST) {
                Integer typeSold = ticketService.getNumberSoldOfType(ticketType);
                ticketInfo.add(new TicketInformationResponse(ticketType, typeSold));
            }
        }
        return ticketInfo;
    }
}
