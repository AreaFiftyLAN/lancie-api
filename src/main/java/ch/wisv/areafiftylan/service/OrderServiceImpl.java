package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.TicketDTO;
import ch.wisv.areafiftylan.dto.TicketInformationResponse;
import ch.wisv.areafiftylan.exception.ImmutableOrderException;
import ch.wisv.areafiftylan.exception.PaymentException;
import ch.wisv.areafiftylan.exception.TicketUnavailableException;
import ch.wisv.areafiftylan.exception.TokenNotFoundException;
import ch.wisv.areafiftylan.model.ExpiredOrder;
import ch.wisv.areafiftylan.model.Order;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.OrderStatus;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.service.repository.ExpiredOrderRepository;
import ch.wisv.areafiftylan.service.repository.OrderRepository;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    OrderRepository orderRepository;
    ExpiredOrderRepository expiredOrderRepository;
    TicketRepository ticketRepository;
    UserService userService;
    PaymentService paymentService;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, UserService userService, TicketRepository ticketRepository,
                            PaymentService paymentService, ExpiredOrderRepository expiredOrderRepository) {
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.userService = userService;
        this.paymentService = paymentService;
        this.expiredOrderRepository = expiredOrderRepository;
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

        for (Order order : orderRepository.findAllByUserUsername(user.getUsername())) {
            if(order.getStatus().equals(OrderStatus.CREATING)){
                throw new RuntimeException("User already created a new Order: " + order.getId());
            }
        }

        // Request a ticket to see if one is available. If a ticket is sold out, the method ends here due to the
        // exception thrown. Else, we'll get a new ticket to add to the order.
        Ticket ticket = this.requestTicketOfType(ticketDTO.getType(), user, ticketDTO.hasPickupService(),
                ticketDTO.isCHMember());

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
            Ticket ticket = this.requestTicketOfType(ticketDTO.getType(), user, ticketDTO.hasPickupService(),
                    ticketDTO.isCHMember());

            order.addTicket(ticket);

            return orderRepository.save(order);
        } else {
            throw new ImmutableOrderException(orderId);
        }
    }

    @Override
    public Ticket requestTicketOfType(TicketType type, User owner, boolean pickupService, boolean chMember) {
        if (ticketRepository.countByType(type) >= type.getLimit()) {
            throw new TicketUnavailableException(type);
        } else {
            Ticket ticket = new Ticket(owner, type, pickupService, chMember);
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
        return paymentService.registerOrder(order);
    }

    @Override
    public Order updateOrderStatus(String orderReference) {

        Order order = paymentService.updateStatus(orderReference);

        // Set all tickets from this Order to valid
        if (order.getStatus().equals(OrderStatus.PAID)) {
            for (Ticket ticket : order.getTickets()) {
                ticket.setValid(true);
                ticketRepository.save(ticket);
            }
        }
        return order;
    }

    @Override
    public Order updateOrderStatus(Long orderId) {
        Order order = orderRepository.findOne(orderId);
        if (!Strings.isNullOrEmpty(order.getReference())) {
            return paymentService.updateStatus(order.getReference());
        } else {
            throw new PaymentException("Order with id " + order + " has not been checked out yet");
        }
    }

    @Override
    public void expireOrder(Order o){
        orderRepository.delete(o);
        ExpiredOrder eo = new ExpiredOrder(o);
        expiredOrderRepository.save(eo);
        o.getTickets().forEach(t -> ticketRepository.delete(t));
    }

    @Override
    public Collection<TicketInformationResponse> getAvailableTickets() {
        Collection<TicketInformationResponse> ticketInfo = new ArrayList<>();

        for (TicketType ticketType : TicketType.values()) {
            Integer typeSold = ticketRepository.countByType(ticketType);
            ticketInfo.add(new TicketInformationResponse(ticketType, typeSold));
        }

        return ticketInfo;
    }
}
