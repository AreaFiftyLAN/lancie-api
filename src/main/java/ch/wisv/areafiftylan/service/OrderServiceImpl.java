package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.OrderDTO;
import ch.wisv.areafiftylan.exception.UserNotFoundException;
import ch.wisv.areafiftylan.model.Order;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class OrderServiceImpl implements OrderService {

    OrderRepository orderRepository;
    UserService userService;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, UserService userService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findOne(id);
    }

    @Override
    public List<Order> getAllOrders() {
        return null;
    }

    @Override
    public Collection<Order> findOrdersByUsername(String username) {
        return null;
    }

    @Override
    public Order addOrder(Long userId, OrderDTO orderDTO) {
        User user = userService.getUserById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        int amountOfTickets = orderDTO.getNumberOfTickets();

        Collection<Ticket> tickets = new HashSet<>();
        for (int i = 0; i < amountOfTickets; i++) {
            //TODO: Make this dependent on orderDTO!
            tickets.add(new Ticket(user, TicketType.FULL, false));
        }

        //TODO: Price calculation
        double price = amountOfTickets * 32.5;
        Order order = new Order(price, user, tickets);

        return order;
    }

    @Override
    public void setPaid(Long id) {
        Order order = orderRepository.findOne(id);
        order.setPaid(true);
    }
}
