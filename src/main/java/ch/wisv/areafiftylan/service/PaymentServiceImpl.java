package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.exception.PaymentException;
import ch.wisv.areafiftylan.exception.UserNotFoundException;
import ch.wisv.areafiftylan.model.Order;
import ch.wisv.areafiftylan.model.util.OrderStatus;
import ch.wisv.areafiftylan.service.repository.OrderRepository;
import com.google.common.base.Strings;
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

    @Value("${a5l.molliekey}")
    String apiKey;

    String method = "ideal";

    @Autowired
    public PaymentServiceImpl(OrderRepository orderRepository, @Value("${a5l.molliekey}") String apiKey){
        this.orderRepository = orderRepository;
        this.mollie = new ClientBuilder().withApiKey(apiKey).build();
    }

    @Override
    public String initOrder(Order order) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("A5LId", order.getId());
        String returnUrl = "https://areafiftylan.nl/ordersuccess";

        CreatePayment payment =
                new CreatePayment(method, order.getPrice(), "Area FiftyLAN Ticket", returnUrl, metadata);
        try {
            ResponseOrError<CreatedPayment> molliePayment = mollie.createPayment(payment);
            if (molliePayment.getSuccess()) {
                order.setReference(molliePayment.getData().getId());
                order.setStatus(OrderStatus.WAITING);
                orderRepository.save(order);
                return molliePayment.getData().getLinks().getPaymentUrl();
            } else {
                ErrorData molliePaymentError = molliePayment.getError();
                HashMap<String, Object> errorMap = (HashMap<String, Object>) molliePaymentError.get("error");
                throw new PaymentException((String) errorMap.get("message"));
            }
        } catch (IOException e) {
            throw new PaymentException("Something went wrong requesting the payment", e.getCause());
        }
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
            throw new PaymentException(e.getMessage());
        }
        return order;
    }

    @Override
    public Order updateStatusByOrderId(Long orderId) {
        Order order = orderRepository.findOne(orderId);
        if (!Strings.isNullOrEmpty(order.getReference())) {
            return updateStatus(order.getReference());
        } else {
            return order;
        }
    }
}
