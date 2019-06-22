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

package ch.wisv.areafiftylan;


import ch.wisv.areafiftylan.products.model.order.Order;
import ch.wisv.areafiftylan.products.model.order.OrderStatus;
import ch.wisv.areafiftylan.products.service.MolliePaymentService;
import ch.wisv.areafiftylan.products.service.PaymentService;
import ch.wisv.areafiftylan.products.service.repository.OrderRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@TestConfiguration
public class ApplicationTest {

    @Bean
    public JavaMailSender mailService() {
        JavaMailSenderImpl mailSender = Mockito.mock(JavaMailSenderImpl.class);
        Mockito.when(mailSender.createMimeMessage()).thenCallRealMethod();
        return mailSender;
    }

    @Bean
    @Primary
    public PaymentService paymentService(OrderRepository orderRepository) {
        MolliePaymentService mockMolliePaymentService = Mockito.mock(MolliePaymentService.class);

        Mockito.when(mockMolliePaymentService.registerOrder(Mockito.any(Order.class))).then(invocation -> {
            Order order = (Order) invocation.getArguments()[0];
            order.setStatus(OrderStatus.PENDING);
            orderRepository.save(order);
            return "http://paymentURL.com";

        });

        Mockito.when(mockMolliePaymentService.getPaymentUrl(Mockito.any()))
                .thenReturn("http://newpaymentURL.com");

        return mockMolliePaymentService;
    }

}