package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.dto.TicketDTO;
import ch.wisv.areafiftylan.exception.ImmutableOrderException;
import ch.wisv.areafiftylan.exception.TicketNotFoundException;
import ch.wisv.areafiftylan.exception.TicketUnavailableException;
import ch.wisv.areafiftylan.model.Order;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.view.View;
import ch.wisv.areafiftylan.service.OrderService;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import static ch.wisv.areafiftylan.util.ResponseEntityBuilder.createResponseEntity;

@RestController
public class OrderRestController {

    private OrderService orderService;

    @Autowired
    public OrderRestController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/orders", method = RequestMethod.GET)
    @JsonView(View.OrderOverview.class)
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
    @JsonView(View.OrderOverview.class)
    public ResponseEntity<?> createOrder(Authentication auth, @RequestBody @Validated TicketDTO ticketDTO) {
        HttpHeaders headers = new HttpHeaders();
        User user = (User) auth.getPrincipal();

        // You can't buy non-buyable Tickts for yourself, this should be done via the createAdminOrder() method.
        if (!ticketDTO.getType().isBuyable()) {
            return createResponseEntity(HttpStatus.FORBIDDEN,
                    "Can't order tickets with type " + ticketDTO.getType().getText());
        }

        Order order = orderService.create(user.getId(), ticketDTO);

        headers.setLocation(
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(order.getId()).toUri());

        return createResponseEntity(HttpStatus.CREATED, headers,
                "Ticket available and order successfully created at " + headers.getLocation(), order);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @JsonView(View.OrderOverview.class)
    @RequestMapping(value = "/users/{userId}/orders", method = RequestMethod.POST)
    public ResponseEntity<?> createAdminOrder(@PathVariable Long userId, @RequestBody @Validated TicketDTO ticketDTO) {
        HttpHeaders headers = new HttpHeaders();
        Order order = orderService.create(userId, ticketDTO);

        headers.setLocation(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orders/{id}").
                buildAndExpand(order.getId()).toUri());

        return createResponseEntity(HttpStatus.CREATED, headers,
                "Ticket available and order successfully created at " + headers.getLocation(), order);
    }

    /**
     * This method handles GET requests on a specific Order.
     *
     * @param orderId Id of the Order that is requested
     *
     * @return The requested Order.
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessOrder(principal, #orderId)")
    @RequestMapping(value = "/orders/{orderId}", method = RequestMethod.GET)
    @JsonView(View.OrderOverview.class)
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
    @PreAuthorize("@currentUserServiceImpl.canAccessOrder(principal, #orderId)")
    @RequestMapping(value = "/orders/{orderId}", method = RequestMethod.POST)
    @JsonView(View.OrderOverview.class)
    public ResponseEntity<?> addToOrder(@PathVariable Long orderId, @RequestBody @Validated TicketDTO ticketDTO) {
        Order modifiedOrder = orderService.addTicketToOrder(orderId, ticketDTO);
        return createResponseEntity(HttpStatus.OK, "Ticket successfully added to your order", modifiedOrder);
    }

    /**
     * Removes a ticket to an existing Order.
     *
     * @param orderId   Id of the Order
     * @param ticketDTO TicketDTO of the Ticket to be removed to the Order
     *
     * @return Message about the result of the request
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessOrder(principal, #orderId)")
    @RequestMapping(value = "/orders/{orderId}", method = RequestMethod.DELETE)
    @JsonView(View.OrderOverview.class)
    public ResponseEntity<?> removeFromOrder(@PathVariable Long orderId, @RequestBody @Validated TicketDTO ticketDTO) {
        Order modifiedOrder = orderService.removeTicketFromOrder(orderId, ticketDTO);
        return createResponseEntity(HttpStatus.OK, "Ticket successfully removed from Order", modifiedOrder);
    }

    /**
     * This method requests payment of the order, locks the order and needs to return information on how to proceed.
     * Depending on PaymentService.
     *
     * @param orderId The order to be paid
     *
     * @return Instructions on how to proceed.
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessOrder(principal, #orderId)")
    @RequestMapping(value = "/orders/{orderId}/checkout", method = RequestMethod.GET)
    public ResponseEntity<?> payOrder(@PathVariable Long orderId) throws URISyntaxException {
        String paymentUrl = orderService.requestPayment(orderId);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI(paymentUrl));

        return createResponseEntity(HttpStatus.OK, headers, "Please go to " + paymentUrl + " to finish your payment");
    }

    /**
     * This endpoint can be used to approve orders, without going through the paymentprovider. You can use this to
     * appoint free tickets or manually approve other orders.
     *
     * @param orderId Order to be approved
     *
     * @return Status message
     */
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/orders/{orderId}/approve", method = RequestMethod.POST)
    public ResponseEntity<?> approveOrder(@PathVariable Long orderId) {
        orderService.adminApproveOrder(orderId);

        return createResponseEntity(HttpStatus.OK, "Order successfully approved");
    }


    /**
     * This method handles the webhook from the payment provider. It requests the status of the order with the given
     * reference
     *
     * @param orderReference Id of the order at the paymentprovider, stored in the reference field
     *
     * @return Statusmessage
     */
    @RequestMapping(value = "/orders/status", method = RequestMethod.POST)
    public ResponseEntity<?> updateOrderStatus(@RequestParam(name = "id") String orderReference) {
        //TODO: Figure out how Mollie sends this request
        orderService.updateOrderStatus(orderReference);
        return createResponseEntity(HttpStatus.OK, "Status is being updated");
    }

    @RequestMapping(value = "/orders/status", method = { RequestMethod.GET,
            RequestMethod.POST }, params = "testByMollie")
    public ResponseEntity<?> handleMollieTestCall() {
        return createResponseEntity(HttpStatus.OK, "Mollie webhook available");
    }

    /**
     * This call allows for manual updating of an order status. It updates the status directly at the paymentprovider,
     * so the status is always current
     *
     * @param orderId OrderId of the Order to be updated
     *
     * @return The Order with an up-to-date status
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessOrder(principal, #orderId)")
    @JsonView(View.OrderOverview.class)
    @RequestMapping(value = "/orders/{orderId}/status", method = RequestMethod.GET)
    public ResponseEntity<?> updateOrderStatusManual(@PathVariable long orderId) {
        Order order = orderService.updateOrderStatus(orderId);
        return createResponseEntity(HttpStatus.OK, "Order status updated", order);
    }

    @ExceptionHandler(TicketUnavailableException.class)
    public ResponseEntity<?> handleTicketUnavailableException(TicketUnavailableException e) {
        return createResponseEntity(HttpStatus.GONE, e.getMessage());
    }

    @ExceptionHandler(ImmutableOrderException.class)
    public ResponseEntity<?> handleWrongOrderStatusException(ImmutableOrderException e) {
        return createResponseEntity(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<?> handleTicketNotFoundException(TicketNotFoundException e) {
        return createResponseEntity(HttpStatus.NOT_MODIFIED, e.getMessage());
    }
}
