package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.Order;
import ch.wisv.areafiftylan.model.util.OrderStatus;
import ch.wisv.areafiftylan.service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

        Collection<Order> allOrdersBeforeDate = orderRepository.findAllByCreationDateTimeBefore(expireBeforeDate);

        List<Order> expiredOrders = allOrdersBeforeDate.stream().filter(isExpired()).collect(Collectors.toList());

        expiredOrders.forEach(o -> orderService.expireOrder(o));
    }

    public static Predicate<Order> isExpired() {
        return o -> o.getStatus().equals(OrderStatus.CREATING) || o.getStatus().equals(OrderStatus.EXPIRED);
    }
}
