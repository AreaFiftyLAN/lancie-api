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

/**
 * Created by sille on 25-12-15.
 */
public interface PaymentService {

    /**
     * Register the order with the payment provider and return the payment URL
     *
     * @param order Order to be registered
     *
     * @return The URL to make the payment
     */
    public String registerOrder(Order order);

    /**
     * Update the status of an Order at the payment provider
     *
     * @param orderReference The Id of the order from the payment provider.
     *
     * @return The updated Order
     */
    public Order updateStatus(String orderReference);
}
