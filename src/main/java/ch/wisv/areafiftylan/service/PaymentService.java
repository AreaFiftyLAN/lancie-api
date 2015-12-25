package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.Order;

/**
 * Created by sille on 25-12-15.
 */
public interface PaymentService {

    public String initOrder(Order order);

    public Order updateStatus(String orderReference);
}
