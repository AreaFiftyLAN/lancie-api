package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.TicketDTO;
import ch.wisv.areafiftylan.model.Order;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.TicketType;

import java.util.Collection;
import java.util.List;

public interface OrderService {

    Order getOrderById(Long id);

    List<Order> getAllOrders();

    Collection<Order> findOrdersByUsername(String username);

    Order create(Long userId, TicketDTO ticketDTO);

    void addTicketToOrder(Long orderId, TicketDTO ticketDTO);

    Ticket requestTicketOfType(TicketType type);

    void transferTicket(User user, String ticketKey);

    void requestPayment(Long orderId);

    void updateOrderStatus(Long orderId);

}
