package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.TicketDTO;
import ch.wisv.areafiftylan.exception.UserNotFoundException;
import ch.wisv.areafiftylan.model.Order;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
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
    public Order create(Long userId, TicketDTO ticketDTO) {
        User user = userService.getUserById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Order order = new Order(user);

        //FIXME: Process the DTO

        return order;
    }

    @Override
    public void addTicketToOrder(Long orderId, TicketDTO ticketDTO) {
        //TODO
    }

    @Override
    public Ticket requestTicketOfType(TicketType type) {
        //TODO
        return null;
    }

    @Override
    public void transferTicket(User user, String ticketKey) {
        //TODO
    }

    @Override
    public void requestPayment(Long orderId) {
        //TODO
    }

    @Override
    public void updateOrderStatus(Long orderId) {
        //TODO
    }
}
