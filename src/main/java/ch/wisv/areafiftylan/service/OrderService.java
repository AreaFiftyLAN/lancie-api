package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.OrderDTO;
import ch.wisv.areafiftylan.model.Order;

import java.util.Collection;
import java.util.List;

public interface OrderService {

    Order getOrderById(Long id);

    List<Order> getAllOrders();

    Collection<Order> findOrdersByUsername(String username);

    Order addOrder(Long userId, OrderDTO orderDTO);

    void setPaid(Long id);

}
