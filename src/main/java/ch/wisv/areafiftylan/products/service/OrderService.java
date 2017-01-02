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

package ch.wisv.areafiftylan.products.service;

import ch.wisv.areafiftylan.products.model.TicketInformationResponse;
import ch.wisv.areafiftylan.products.model.order.Order;

import java.util.Collection;
import java.util.List;

public interface OrderService {

    Order getOrderById(Long id);

    List<Order> getAllOrders();

    Collection<Order> findOrdersByUsername(String username);

    List<Order> getOpenOrders(String username);

    /**
     * Create a new Order containing a single ticket
     */
    Order create(String type, List<String> options);

    /**
     * Add a ticket to an order. Checks if a ticket is available first
     */
    Order addTicketToOrder(Long orderId, String type, List<String> options);

    Order assignOrderToUser(Long orderId, String username);

    /**
     * Removes a ticket with the given DTO from an order. Throws a NotFoundException when a ticket with such a DTO can't
     * be found
     *
     * @param orderId       The Id of the Order from which the tickets have to be removed
     * @param ticketId      The Id of the Ticket to remove from the order
     *
     * @return The updated Order where ticket has been removed if present
     */
    Order removeTicketFromOrder(Long orderId, Long ticketId);

    /**
     * Register the order with the payment provider
     *
     * @return The URL for payment
     */
    String requestPayment(Long orderId);

    Order updateOrderStatusByReference(String orderReference);

    Order updateOrderStatusByOrderId(Long orderId);

    /**
     * Manually approve an order, without going through the paymentprovider. This method sets the Orderstatus to PAID
     * and validates all the tickets
     */
    void adminApproveOrder(Long orderId);

    /**
     * Expire an order which will remove the order from the orders table and enter a relevant entry in the expired
     * orders table
     */
    void expireOrder(Order o);

    /**
     * This method returns an overview of all available tickets, and information about them
     *
     * @return A collection of TicketInformation objects
     */
    Collection<TicketInformationResponse> getAvailableTickets();
}
