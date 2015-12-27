package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.exception.OrderNotFoundException;
import ch.wisv.areafiftylan.exception.PaymentException;
import ch.wisv.areafiftylan.exception.PaymentServiceConnectionException;
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
    public String registerOrder(Order order) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("A5LId", order.getId());
        //TODO: Replace this with a config-based URL
        String returnUrl = "https://areafiftylan.nl/ordersuccess";

        CreatePayment payment =
                new CreatePayment(method, order.getPrice(), "Area FiftyLAN Ticket", returnUrl, metadata);

        //First try is for IOExceptions coming from the Mollie Client.
        try {
            // Create the payment over at Mollie
            ResponseOrError<CreatedPayment> molliePayment = mollie.createPayment(payment);

            if (molliePayment.getSuccess()) {
                // All good, update the order
                updateOrder(order, molliePayment);
                return molliePayment.getData().getLinks().getPaymentUrl();
            } else {
                // Mollie returned an error.
                handleMollieError(molliePayment);
                return null;
            }
        } catch (IOException e) {
            // This indicates the HttpClient encountered some error
            throw new PaymentException("Could not connect to the Paymentprovider");
        }
    }

    private void updateOrder(Order order, ResponseOrError<CreatedPayment> molliePayment) {
        // Insert the Mollie ID for future reference
        order.setReference(molliePayment.getData().getId());
        order.setStatus(OrderStatus.WAITING);

        // Save the changes to the order
        orderRepository.save(order);
    }

    @Override
    public Order updateStatus(String orderReference) {
        Order order = orderRepository.findByReference(orderReference)
                .orElseThrow(() -> new OrderNotFoundException("Order with reference " + orderReference + " not found"));

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
                // Order status could not be updated for some reason. Return the original order
                handleMollieError(molliePaymentStatus);
                return order;
            }
        } catch (IOException e) {
            // This indicates the HttpClient encountered some error
            throw new PaymentServiceConnectionException(e.getMessage());
        }
    }

    private void handleMollieError(ResponseOrError<?> mollieResponseWithError) {
        // Some error occured, but connection to Mollie succeeded, which means they have something to say.
        ErrorData molliePaymentError = mollieResponseWithError.getError();

        // Make the compiler shut up, this is something stupid in the Mollie API Client
        @SuppressWarnings("unchecked") HashMap<String, Object> errorMap =
                (HashMap<String, Object>) molliePaymentError.get("error");
        throw new PaymentException((String) errorMap.get("message"));
    }

}
