/*
 * Copyright (c) 2018  W.I.S.V. 'Christiaan Huygens'
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

import ch.wisv.areafiftylan.exception.OrderNotFoundException;
import ch.wisv.areafiftylan.exception.PaymentException;
import ch.wisv.areafiftylan.exception.PaymentServiceConnectionException;
import ch.wisv.areafiftylan.products.model.order.Order;
import ch.wisv.areafiftylan.products.model.order.OrderStatus;
import ch.wisv.areafiftylan.products.service.repository.OrderRepository;
import nl.stil4m.mollie.Client;
import nl.stil4m.mollie.ClientBuilder;
import nl.stil4m.mollie.ResponseOrError;
import nl.stil4m.mollie.domain.CreatePayment;
import nl.stil4m.mollie.domain.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class MolliePaymentService implements PaymentService {

    private final OrderRepository orderRepository;

    @Value("${a5l.molliekey:null}")
    String apiKey;

    @Value("${a5l.paymentReturnUrl}")
    String returnUrl;

    @Autowired
    public MolliePaymentService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public String registerOrder(Order order) {
        Client mollie =  new ClientBuilder().withApiKey(apiKey).build();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("A5LId", order.getId());

        Optional<String> method = Optional.of("ideal");
        CreatePayment payment = new CreatePayment(method, (double) order.getAmount(), "Area FiftyLAN Ticket",
                returnUrl + "?order=" + order.getId(), Optional.empty(), metadata);

        //First try is for IOExceptions coming from the Mollie Client.
        try {
            // Create the payment over at Mollie
            ResponseOrError<Payment> molliePayment = mollie.payments().create(payment);

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

    private void updateOrder(Order order, ResponseOrError<Payment> molliePayment) {
        // Insert the Mollie ID for future reference
        order.setReference(molliePayment.getData().getId());
        order.setStatus(OrderStatus.PENDING);

        // Save the changes to the order
        orderRepository.saveAndFlush(order);
    }

    @Override
    public Order updateStatus(String orderReference) {
        Client mollie =  new ClientBuilder().withApiKey(apiKey).build();

        Order order = orderRepository.findByReference(orderReference)
                .orElseThrow(() -> new OrderNotFoundException("Order with reference " + orderReference + " not found"));

        // This try is for the Mollie API internal HttpClient
        try {
            // Request a payment from Mollie
            ResponseOrError<Payment> molliePaymentStatus = mollie.payments().get(orderReference);

            // If the request was a success, we can update the order
            if (molliePaymentStatus.getSuccess()) {
                // There are a couple of possible statuses. Enum would have been nice. We select a couple of relevant
                // statuses to translate to our own status.
                switch (molliePaymentStatus.getData().getStatus()) {
                    case "pending": {
                        order.setStatus(OrderStatus.PENDING);
                        break;
                    }
                    case "cancelled": {
                        order.setStatus(OrderStatus.CANCELLED);
                        break;
                    }
                    case "expired": {
                        order.setStatus(OrderStatus.EXPIRED);
                        break;
                    }
                    case "paid": {
                        order.setStatus(OrderStatus.PAID);
                        break;
                    }
                    case "paidout": {
                        order.setStatus(OrderStatus.PAID);
                        break;
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

    @Override
    public String getPaymentUrl(String orderReference) {
        Client mollie =  new ClientBuilder().withApiKey(apiKey).build();

        try {
            ResponseOrError<Payment> paymentResponseOrError = mollie.payments().get(orderReference);

            if (paymentResponseOrError.getSuccess()) {
                return paymentResponseOrError.getData().getLinks().getPaymentUrl();
            } else {
                handleMollieError(paymentResponseOrError);
            }

        } catch (IOException e) {
            throw new PaymentServiceConnectionException(e.getMessage());
        }
        throw new PaymentException("Can't retrieve Payment URL for Order " + orderReference);
    }

    private void handleMollieError(ResponseOrError<?> mollieResponseWithError) {
        // Some error occured, but connection to Mollie succeeded, which means they have something to say.
        Map molliePaymentError = mollieResponseWithError.getError();

        // Make the compiler shut up, this is something stupid in the Mollie API Client
        Map error = (Map) molliePaymentError.get("error");
        throw new PaymentException((String) error.get("message"));
    }
}
