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

import ch.wisv.areafiftylan.products.model.TicketDTO;
import ch.wisv.areafiftylan.products.model.TicketInformationResponse;
import ch.wisv.areafiftylan.products.model.Order;

import java.util.Collection;
import java.util.List;

public interface OrderService {

    Order getOrderById(Long id);

    List<Order> getAllOrders();

    Collection<Order> findOrdersByUsername(String username);

    List<Order> getOpenOrders(String username);

    /**
     * Create an order with at least one Ticket.
     *
     * @param userId    User which orders the ticket
     * @param ticketDTO The ticket that is being ordered
     *
     * @return The created order
     */
    Order create(Long userId, TicketDTO ticketDTO);

    /**
     * Add a ticket to an order. Checks if a ticket is available first
     *
     * @param orderId   Order to which the ticket should be added
     * @param ticketDTO DTO of the ticket to be added
     *
     * @return The updated Order
     */
    Order addTicketToOrder(Long orderId, TicketDTO ticketDTO);

    /**
     * Removes a ticket with the given DTO from an order. Throws a NotFoundException when a ticket with such a DTO can't
     * be found
     *
     * @param orderId   Order from which the Ticket should be removed
     * @param ticketDTO Ticket which should be removed from the Order
     *
     * @return The modified Order
     */
    Order removeTicketFromOrder(Long orderId, TicketDTO ticketDTO);

    /**
     * Register the order with the payment provider
     *
     * @param orderId The order to be checked out
     *
     * @return The URL for payment
     */
    String requestPayment(Long orderId);

    Order updateOrderStatus(String orderReference);

    Order updateOrderStatus(Long orderId);

    /**
     * Manually approve an order, without going through the paymentprovider. This method sets the Orderstatus to PAID
     * and validates all the tickets
     *
     * @param orderId Order to be approved
     */
    void adminApproveOrder(Long orderId);

    /**
     * Expire an order which will remove the order from the orders table and enter a relevant entry in the expired
     * orders table
     *
     * @param o The order to expire
     */
    void expireOrder(Order o);

    /**
     * This method returns an overview of all available tickets, and information about them
     *
     * @return A collection of TicketInformation objects
     */
    Collection<TicketInformationResponse> getAvailableTickets();
}
