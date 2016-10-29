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

package ch.wisv.areafiftylan;


import ch.wisv.areafiftylan.products.model.Order;
import ch.wisv.areafiftylan.products.model.OrderStatus;
import ch.wisv.areafiftylan.products.service.MolliePaymentService;
import ch.wisv.areafiftylan.products.service.OrderRepository;
import ch.wisv.areafiftylan.products.service.PaymentService;
import ch.wisv.areafiftylan.utils.mail.MailService;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@ActiveProfiles("test")
public class ApplicationTest {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationTest.class, args);
    }


    @Bean
    @Primary
    public MailService mailService() {
        return Mockito.mock(MailService.class);
    }

    @Bean
    @Primary
    public PaymentService paymentService(OrderRepository orderRepository) {
        MolliePaymentService mockMolliePaymentService = Mockito.mock(MolliePaymentService.class);

        Mockito.when(mockMolliePaymentService.registerOrder(Mockito.any(Order.class))).then(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Order order = (Order) invocation.getArguments()[0];
                order.setStatus(OrderStatus.WAITING);
                orderRepository.save(order);
                return "http://paymentURL.com";

            }
        });
        return mockMolliePaymentService;
    }
}