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
public class MolliePaymentService implements PaymentService {

    OrderRepository orderRepository;

    Client mollie;

    @Value("${a5l.molliekey}")
    String apiKey;

    String method = "ideal";

    @Autowired
    public MolliePaymentService(OrderRepository orderRepository, @Value("${a5l.molliekey}") String apiKey) {
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

        //First try is for IOExceptions coming from the Mollie Client.
        try {
            // Create the payment over at Mollie
            ResponseOrError<CreatedPayment> molliePayment = mollie.createPayment(payment);

            if (molliePayment.getSuccess()) {
                // All good, update the order
                // Insert the Mollie ID for future reference
                order.setReference(molliePayment.getData().getId());
                order.setStatus(OrderStatus.WAITING);

                // Save the changes to the order
                orderRepository.save(order);

                // Return the payment URL so the User can pay.
                return molliePayment.getData().getLinks().getPaymentUrl();
            } else {
                // Some error occured, but connection to Mollie succeeded, which means they have something to say.
                ErrorData molliePaymentError = molliePayment.getError();

                // Make the compiler shut up, this is something stupid in the Mollie API Client
                @SuppressWarnings("unchecked")
                HashMap<String, Object> errorMap = (HashMap<String, Object>) molliePaymentError.get("error");
                //TODO: This should be a BAD REQUEST exception
                throw new PaymentException((String) errorMap.get("message"));
            }
        } catch (IOException e) {
            // This indicates the HttpClient encountered some error
            // TODO: Make this throw a different Status 500 Exception.
            throw new PaymentException("Something went wrong requesting the payment", e.getCause());
        }
    }

    @Override
    public Order updateStatus(String orderReference) {
        // TODO: Make this throw a proper BAD REQUEST exception
        Order order = orderRepository.findByReference(orderReference)
                .orElseThrow(() -> new UserNotFoundException("Order with reference " + orderReference + " not found"));

        // This try is for the Mollie API internal HttpClient
        try {
            // Request a payment from Mollie
            ResponseOrError<PaymentStatus> molliePaymentStatus = mollie.getPaymentStatus(apiKey, orderReference);

            // If the request was a success, we can update the order
            if (molliePaymentStatus.getSuccess()) {
                // There are a couple of possible statuses. Enum would have been nice. We select a couple of relevant
                // statuses to translate to our own status.
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
                    case "paidout": {
                        order.setStatus(OrderStatus.PAID);
                    }
                }
                return orderRepository.save(order);
            } else {
                // Some error occured, but connection to Mollie succeeded, which means they have something to say.
                ErrorData molliePaymentError = molliePaymentStatus.getError();

                // Make the compiler shut up, this is something stupid in the Mollie API Client
                @SuppressWarnings("unchecked") HashMap<String, Object> errorMap =
                        (HashMap<String, Object>) molliePaymentError.get("error");
                //TODO: This should be a BAD REQUEST exception
                throw new PaymentException((String) errorMap.get("message"));
            }
        } catch (IOException e) {
            // This indicates the HttpClient encountered some error
            // TODO: Make this throw a different Status 500 Exception.
            throw new PaymentException(e.getMessage());
        }
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
