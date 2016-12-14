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


import ch.wisv.areafiftylan.products.model.TicketOption;
import ch.wisv.areafiftylan.products.model.TicketType;
import ch.wisv.areafiftylan.products.model.order.Order;
import ch.wisv.areafiftylan.products.model.order.OrderStatus;
import ch.wisv.areafiftylan.products.service.MolliePaymentService;
import ch.wisv.areafiftylan.products.service.PaymentService;
import ch.wisv.areafiftylan.products.service.repository.OrderRepository;
import ch.wisv.areafiftylan.products.service.repository.TicketOptionRepository;
import ch.wisv.areafiftylan.products.service.repository.TicketTypeRepository;
import ch.wisv.areafiftylan.utils.mail.MailService;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

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

        Mockito.when(mockMolliePaymentService.registerOrder(Mockito.any(Order.class))).then(invocation -> {
            Order order = (Order) invocation.getArguments()[0];
            order.setStatus(OrderStatus.PENDING);
            orderRepository.save(order);
            return "http://paymentURL.com";

        });
        return mockMolliePaymentService;
    }

    @Component
    public class TestRunner implements CommandLineRunner {


        private final TicketOptionRepository ticketOptionRepository;
        private final TicketTypeRepository ticketTypeRepository;

        @Autowired
        public TestRunner(TicketOptionRepository ticketOptionRepository, TicketTypeRepository ticketTypeRepository) {
            this.ticketOptionRepository = ticketOptionRepository;
            this.ticketTypeRepository = ticketTypeRepository;
        }

        @Override
        public void run(String... evt) throws Exception {
            TicketOption chMember = ticketOptionRepository.save(new TicketOption("chMember", -5F));
            TicketOption pickupService = ticketOptionRepository.save(new TicketOption("pickupService", 2.5F));
            TicketOption extraOption = ticketOptionRepository.save(new TicketOption("extraOption", 10F));

            TicketType ticketType =
                    new TicketType("test", "Testing Ticket", 30F, 0, LocalDateTime.now().plusDays(1), true);
            ticketType.addPossibleOption(chMember);
            ticketType.addPossibleOption(pickupService);
            ticketTypeRepository.save(ticketType);
        }
    }
}