package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.Order;
import ch.wisv.areafiftylan.service.repository.ExpiredOrderRepository;
import ch.wisv.areafiftylan.service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Created by beer on 28-12-15.
 *
 * Class with all scheduled tasks
 */
@Component
public class TaskScheduler {
    private final int ORDER_STAY_ALIVE_MINUTES = 30;
    private final int ORDER_EXPIRY_CHECK_INTERVAL_SECONDS = 60;

    private OrderRepository orderRepository;
    private OrderService orderService;

    @Autowired
    public TaskScheduler(OrderRepository orderRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }


    @Scheduled(fixedRate = ORDER_EXPIRY_CHECK_INTERVAL_SECONDS * 1000)
    public void ExpireOrders(){
        LocalDateTime expireBeforeDate = LocalDateTime.now().minusMinutes(ORDER_STAY_ALIVE_MINUTES);

        Collection<Order> ordersToExpire = orderRepository.findAllByCreationDateTimeBefore(expireBeforeDate);

        ordersToExpire.forEach(o -> orderService.expireOrder(o));
    }
}
