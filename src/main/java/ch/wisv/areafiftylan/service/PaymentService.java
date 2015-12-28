package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.Order;

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
