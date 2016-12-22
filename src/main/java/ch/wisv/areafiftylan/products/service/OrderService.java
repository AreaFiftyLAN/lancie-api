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

import ch.wisv.areafiftylan.products.model.Order;
import ch.wisv.areafiftylan.products.model.TicketInformationResponse;
import ch.wisv.areafiftylan.products.model.TicketType;

import java.util.Collection;
import java.util.List;

public interface OrderService {

    Order getOrderById(Long id);

    List<Order> getAllOrders();

    Collection<Order> findOrdersByUsername(String username);

    List<Order> getOpenOrders(String username);

    /**
     * Create a new Order containing a single ticket
     *
     * @param type          Ticket Type
     * @param pickupService Ticket includes pickup service
     * @param chMember      Ticket includes chMember
     *
     * @return The newly created order
     */
    Order create(TicketType type, boolean pickupService, boolean chMember);

    /**
     * Add a ticket to an order. Checks if a ticket is available first
     *
     * @param orderId
     * @param type          Ticket Type
     * @param pickupService Ticket includes pickup service
     * @param chMember      Ticket includes chMember
     *
     * @return The order including the newly added ticket
     */
    Order addTicketToOrder(Long orderId, TicketType type, boolean pickupService, boolean chMember);

    Order assignOrderToUser(Long orderId, String username);

    /**
     * Removes a ticket with the given DTO from an order. Throws a NotFoundException when a ticket with such a DTO can't
     * be found
     *
     * @param orderId       The Id of the Order from which the tickets have to be removed
     * @param type          The Type of the ticket to be removed
     * @param pickupService The pickupservice bool of the ticket to be removed
     * @param chMember      The chMember bool of the ticket to be removed
     *
     * @return The updated Order where ticket has been removed if present
     */
    Order removeTicketFromOrder(Long orderId, TicketType type, boolean pickupService, boolean chMember);

    /**
     * Register the order with the payment provider
     *
     * @param orderId The order to be checked out
     *
     * @return The URL for payment
     */
    String requestPayment(Long orderId);

    Order updateOrderStatusByReference(String orderReference);

    Order updateOrderStatusByOrderId(Long orderId);

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
