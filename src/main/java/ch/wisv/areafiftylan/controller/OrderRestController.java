package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.TicketDTO;
import ch.wisv.areafiftylan.exception.TicketUnavailableException;
import ch.wisv.areafiftylan.model.Order;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.OrderService;
import ch.wisv.areafiftylan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

@RestController
public class OrderRestController {

    OrderService orderService;

    UserService userService;

    @Autowired
    public OrderRestController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/orders", method = RequestMethod.GET)
    public Collection<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    /**
     * When a User does a POST request to /orders, a new Order is created. The requestbody is a TicketDTO, so an order
     * always contains at least one ticket. Optional next tickets should be added to the order by POSTing to the
     * location provided.
     *
     * @param auth      The User that is currently logged in
     * @param ticketDTO Object containing information about the Ticket that is being ordered.
     *
     * @return A message informing about the result of the request
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/orders", method = RequestMethod.POST)
    public ResponseEntity<?> createOrder(Authentication auth, @RequestBody TicketDTO ticketDTO) {
        HttpHeaders headers = new HttpHeaders();
        User user = (User) auth.getPrincipal();

        Order order = orderService.create(user.getId(), ticketDTO);

        headers.setLocation(
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(order.getId()).toUri());

        return createResponseEntity(HttpStatus.OK, headers,
                "Ticket available and order successfully created at " + headers.getLocation(), order);
    }

    /**
     * This method handles GET requests on a specific Order.
     *
     * @param orderId Id of the Order that is requested
     *
     * @return The requested Order.
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessOrder(principal, #userId)")
    @RequestMapping(value = "/orders/{orderId}", method = RequestMethod.GET)
    public Order getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }

    /**
     * Adds a ticket to an existing Order. It's possible to buy serveral ticket at once. After the order has been
     * created, tickets can be added by POSTing more TicketDTOs to this location.
     *
     * @param orderId   Id of the Order
     * @param ticketDTO TicketDTO of the Ticket to be added to the Order
     *
     * @return Message about the result of the request
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessOrder(principal, #userId)")
    @RequestMapping(value = "/orders/{orderId}", method = RequestMethod.POST)
    public ResponseEntity<?> addToOrder(@PathVariable Long orderId, @RequestBody TicketDTO ticketDTO) {
        orderService.addTicketToOrder(orderId, ticketDTO);
        return createResponseEntity(HttpStatus.OK, "Ticket successfully added to your order");
    }

    /**
     * This method requests payment of the order, locks the order and needs to return information on how to proceed.
     * Depending on PaymentService.
     *
     * @param orderId The order to be paid
     *
     * @return Instructions on how to proceed.
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessOrder(principal, #userId)")
    @RequestMapping(value = "/orders/{orderId}/checkout", method = RequestMethod.GET)
    public ResponseEntity<?> payOrder(@PathVariable Long orderId) {
        //TODO: Implement Paymentprovider calls here.
        orderService.requestPayment(orderId);

        return createResponseEntity(HttpStatus.OK, "Please go to ... to finish your payment");
    }

    @RequestMapping(value = "/orders/status", method = RequestMethod.POST)
    public ResponseEntity<?> addToOrder(@RequestParam String orderId) {
        //TODO: Implement webhook for payment provider
        return null;
    }


    @ExceptionHandler(TicketUnavailableException.class)
    public ResponseEntity<?> handleTicketUnavailableException(TicketUnavailableException e) {
        return createResponseEntity(HttpStatus.GONE, e.getMessage());
    }
}
