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

package ch.wisv.areafiftylan.products.controller;

import ch.wisv.areafiftylan.exception.ImmutableOrderException;
import ch.wisv.areafiftylan.exception.OrderNotFoundException;
import ch.wisv.areafiftylan.exception.TicketNotFoundException;
import ch.wisv.areafiftylan.exception.TicketUnavailableException;
import ch.wisv.areafiftylan.products.model.TicketDTO;
import ch.wisv.areafiftylan.products.model.order.Order;
import ch.wisv.areafiftylan.products.service.OrderService;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.utils.view.View;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@Log4j2
@RequestMapping("/orders")
public class OrderRestController {

    private final OrderService orderService;

    @Autowired
    public OrderRestController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @JsonView(View.OrderOverview.class)
    public Collection<Order> getAllOrders() {
        return orderService.getAllOrders();

    }

    /**
     * When a User does a POST request to /orders, a new Order is created. The requestbody is a TicketDTO, so an order
     * always contains at least one ticket. Optional next tickets should be added to the order by POSTing to the
     * location provided.
     *
     * @param ticketDTO Object containing information about the Ticket that is being ordered.
     *
     * @return A message informing about the result of the request
     */
    @PostMapping
    @JsonView(View.OrderOverview.class)
    public ResponseEntity<?> createOrder(@RequestBody @Validated TicketDTO ticketDTO) {
        HttpHeaders headers = new HttpHeaders();

        Order order = orderService.create(ticketDTO.getType(), ticketDTO.getOptions());

        headers.setLocation(
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(order.getId()).toUri());

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
    @GetMapping("/{orderId}")
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
    @PostMapping("/{orderId}")
    @JsonView(View.OrderOverview.class)
    public ResponseEntity<?> addToOrder(@PathVariable Long orderId, @RequestBody @Validated TicketDTO ticketDTO) {
        Order modifiedOrder = orderService.addTicketToOrder(orderId, ticketDTO.getType(), ticketDTO.getOptions());
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
    @DeleteMapping("/{orderId}/{ticketId}")
    @JsonView(View.OrderOverview.class)
    public ResponseEntity<?> removeFromOrder(@PathVariable Long orderId, @PathVariable Long ticketId) {
        Order modifiedOrder = orderService.removeTicketFromOrder(orderId, ticketId);
        return createResponseEntity(HttpStatus.OK, "Ticket successfully removed from Order", modifiedOrder);
    }

    /**
     * Assign a User to a previously anonymous Order. The currently assigned user is assigned to the order specified in
     * the path. This is required before an order can be paid.
     *
     * @param user    The logged in user
     * @param orderId Id of the Order to be assigned to the currently logged in user
     *
     * @return The assigned order
     */
    @PreAuthorize("isAuthenticated() and @currentUserServiceImpl.canAccessOrder(principal, #orderId)")
    @PostMapping("/{orderId}/assign")
    @JsonView(View.OrderOverview.class)
    public ResponseEntity<?> assignOrderToUser(@AuthenticationPrincipal User user, @PathVariable Long orderId) {
        Order assignedOrder = orderService.assignOrderToUser(orderId, user.getEmail());
        return createResponseEntity(HttpStatus.OK, "Order successfully attached to User", assignedOrder);
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
    @PostMapping("/{orderId}/checkout")
    public ResponseEntity<?> payOrder(@PathVariable Long orderId) throws URISyntaxException {
        String paymentUrl = orderService.requestPayment(orderId);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI(paymentUrl));

        return createResponseEntity(HttpStatus.OK, headers, "Please go to the url to finish your payment", paymentUrl);
    }

    /**
     * This method gets the paymentURL for an order that was already registered at the paymentprovider.
     *
     * @param orderId The order to be continued
     *
     * @return The paymentURL from the paymentprovider
     */
    @PreAuthorize("@currentUserServiceImpl.canAccessOrder(principal, #orderId)")
    @GetMapping(value = "/{orderId}/url")
    public ResponseEntity<?> getPaymentURL(@PathVariable Long orderId) throws URISyntaxException {
        String paymentUrl = orderService.getPaymentUrl(orderId);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI(paymentUrl));

        return createResponseEntity(HttpStatus.OK, headers, "Please go to the url to finish your payment", paymentUrl);
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
    @PostMapping("/{orderId}/approve")
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
    @PostMapping("/status")
    public ResponseEntity<?> updateOrderStatus(@RequestParam(name = "id") String orderReference) {
        log.log(Level.getLevel("A5L"), "Incoming paymentprovicer webhook for reference: {}", orderReference);
        try {
            orderService.updateOrderStatusByReference(orderReference);
        } catch (OrderNotFoundException e) {
            log.log(Level.getLevel("A5L"), "Paymentprovider webhook reference could not be found: {}", orderReference);
        }
        return createResponseEntity(HttpStatus.OK, "Status is being updated");
    }

    @RequestMapping(value = "/status", method = { RequestMethod.GET, RequestMethod.POST }, params = "testByMollie")
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
    @GetMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatusManual(@PathVariable long orderId) {
        Order order = orderService.updateOrderStatusByOrderId(orderId);
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
