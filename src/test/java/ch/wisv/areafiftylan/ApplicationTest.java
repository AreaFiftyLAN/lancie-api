package ch.wisv.areafiftylan;


import ch.wisv.areafiftylan.model.Order;
import ch.wisv.areafiftylan.model.util.OrderStatus;
import ch.wisv.areafiftylan.service.MailService;
import ch.wisv.areafiftylan.service.MolliePaymentService;
import ch.wisv.areafiftylan.service.PaymentService;
import ch.wisv.areafiftylan.service.repository.OrderRepository;
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