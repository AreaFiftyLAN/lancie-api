package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.TicketDTO;
import ch.wisv.areafiftylan.exception.ImmutableOrderException;
import ch.wisv.areafiftylan.exception.TicketUnavailableException;
import ch.wisv.areafiftylan.exception.TokenNotFoundException;
import ch.wisv.areafiftylan.model.Order;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.OrderStatus;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.service.repository.OrderRepository;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    OrderRepository orderRepository;
    TicketRepository ticketRepository;
    UserService userService;
    PaymentService paymentService;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, UserService userService, TicketRepository ticketRepository,
                            PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.userService = userService;
        this.paymentService = paymentService;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findOne(id);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Collection<Order> findOrdersByUsername(String username) {
        return orderRepository.findAllByUserUsername(username);
    }

    @Override
    public Order create(Long userId, TicketDTO ticketDTO) {
        User user = userService.getUserById(userId);

        // Request a ticket to see if one is available. If a ticket is sold out, the method ends here due to the
        // exception thrown. Else, we'll get a new ticket to add to the order.
        Ticket ticket = this.requestTicketOfType(ticketDTO.getType(), user, ticketDTO.hasPickupService());

        Order order = new Order(user);

        order.addTicket(ticket);

        return orderRepository.save(order);
    }

    @Override
    public Order addTicketToOrder(Long orderId, TicketDTO ticketDTO) {
        Order order = orderRepository.getOne(orderId);

        if (order.getStatus().equals(OrderStatus.CREATING)) {
            User user = order.getUser();

            // Request a ticket to see if one is available. If a ticket is sold out, the method ends here due to the
            // exception thrown. Else, we'll get a new ticket to add to the order.
            Ticket ticket = this.requestTicketOfType(ticketDTO.getType(), user, ticketDTO.hasPickupService());

            order.addTicket(ticket);

            return orderRepository.save(order);
        } else {
            throw new ImmutableOrderException(orderId);
        }
    }

    @Override
    public Ticket requestTicketOfType(TicketType type, User owner, boolean pickupService) {
        if (ticketRepository.countByType(type) >= type.getLimit()) {
            throw new TicketUnavailableException(type);
        } else {
            Ticket ticket = new Ticket(owner, type, pickupService);
            return ticketRepository.save(ticket);
        }
    }

    @Override
    public void transferTicket(User user, String ticketKey) {
        Ticket ticket = ticketRepository.findByKey(ticketKey).orElseThrow(() -> new TokenNotFoundException(ticketKey));

        if (ticket.isLockedForTransfer()) {
            ticket.setPreviousOwner(ticket.getOwner());

            ticket.setOwner(user);

            ticket.setLockedForTransfer(true);

            ticketRepository.save(ticket);
        } else {
            //TODO: Deal with invalid transfer attempt
        }
    }

    @Override
    public String requestPayment(Long orderId) {
        Order order = orderRepository.findOne(orderId);
        order.setStatus(OrderStatus.WAITING);
        String paymentUrl = paymentService.initOrder(order);
        orderRepository.save(order);
        return paymentUrl;
    }

    @Override
    public Order updateOrderStatus(String orderReference) {
        return paymentService.updateStatus(orderReference);
    }

    @Override
    public Order updateOrderStatus(Long orderId) {
        return paymentService.updateStatusByOrderId(orderId);
    }
}
