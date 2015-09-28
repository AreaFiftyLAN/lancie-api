package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.OrderDTO;
import ch.wisv.areafiftylan.model.Order;
import ch.wisv.areafiftylan.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
public class OrderRestController {

    OrderService orderService;

    @Autowired
    public OrderRestController(OrderService orderService) {
        this.orderService = orderService;
    }

    @RequestMapping(value = "/orders", method = RequestMethod.GET)
    public Collection<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @RequestMapping(value = "/orders/{orderId}", method = RequestMethod.GET)
    public Order getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }

    @RequestMapping(value = "/users/{userId}/order", method = RequestMethod.POST)
    Order addOrder(@PathVariable Long userId, @RequestBody OrderDTO orderDTO) {
        return orderService.addOrder(userId, orderDTO);
    }
}
