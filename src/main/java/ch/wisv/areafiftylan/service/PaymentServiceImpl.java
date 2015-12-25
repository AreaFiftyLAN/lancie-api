package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.exception.UserNotFoundException;
import ch.wisv.areafiftylan.model.Order;
import ch.wisv.areafiftylan.model.util.OrderStatus;
import ch.wisv.areafiftylan.service.repository.OrderRepository;
import nl.stil4m.mollie.Client;
import nl.stil4m.mollie.ClientBuilder;
import nl.stil4m.mollie.ResponseOrError;
import nl.stil4m.mollie.domain.CreatePayment;
import nl.stil4m.mollie.domain.CreatedPayment;
import nl.stil4m.mollie.domain.ErrorData;
import nl.stil4m.mollie.domain.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sille on 25-12-15.
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    OrderRepository orderRepository;

    Client mollie;

    @Value("mollie.testkey")
    String apiKey;

    String method = "ideal";

    private final String RETURN_URL = "https://areafiftylan.nl/ordersuccess";

    @Autowired
    public PaymentServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.mollie = new ClientBuilder().withApiKey(apiKey).build();
    }

    @Override
    public String initOrder(Order order) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("A5LId", order.getId());
        CreatePayment payment =
                new CreatePayment(method, order.getPrice(), "Area FiftyLAN Ticket", RETURN_URL, metadata);
        try {
            ResponseOrError<CreatedPayment> molliePayment = mollie.createPayment(payment);
            if (molliePayment.getSuccess()) {
                order.setReference(molliePayment.getData().getId());
                return molliePayment.getData().getLinks().getPaymentUrl();
            } else {
                ErrorData molliePaymentError = molliePayment.getError();
                throw new RuntimeException("Payment Creation Failed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Order updateStatus(String orderReference) {
        Order order = orderRepository.findByReference(orderReference)
                .orElseThrow(() -> new UserNotFoundException("Order with reference " + orderReference + " not found"));
        try {
            ResponseOrError<PaymentStatus> molliePaymentStatus = mollie.getPaymentStatus(apiKey, orderReference);
            if (molliePaymentStatus.getSuccess()) {
                switch (molliePaymentStatus.getData().getStatus()) {
                    case "cancelled": {
                        order.setStatus(OrderStatus.EXPIRED);
                    }
                    case "expired": {
                        order.setStatus(OrderStatus.EXPIRED);
                    }
                    case "paid": {
                        order.setStatus(OrderStatus.PAID);
                    }
                }
                order = orderRepository.save(order);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return order;
    }
}
