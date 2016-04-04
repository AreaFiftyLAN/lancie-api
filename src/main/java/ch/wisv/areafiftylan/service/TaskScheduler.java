package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.Order;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.OrderStatus;
import ch.wisv.areafiftylan.security.token.VerificationToken;
import ch.wisv.areafiftylan.service.repository.OrderRepository;
import ch.wisv.areafiftylan.service.repository.UserRepository;
import ch.wisv.areafiftylan.service.repository.token.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by beer on 28-12-15.
 * <p>
 * Class with all scheduled tasks
 */
@Component
public class TaskScheduler {
    private final int ORDER_STAY_ALIVE_MINUTES = 30;
    private final int ORDER_EXPIRY_CHECK_INTERVAL_SECONDS = 60;

    private final int USER_CLEANUP_CHECK_INTERVAL_MINUTES = 60;

    private OrderRepository orderRepository;
    private OrderService orderService;

    private UserRepository userRepository;
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    public TaskScheduler(OrderRepository orderRepository, OrderService orderService, VerificationTokenRepository verificationTokenRepository,
                            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
    }


    @Scheduled(fixedRate = ORDER_EXPIRY_CHECK_INTERVAL_SECONDS * 1000)
    public void ExpireOrders() {
        LocalDateTime expireBeforeDate = LocalDateTime.now().minusMinutes(ORDER_STAY_ALIVE_MINUTES);

        Collection<Order> allOrdersBeforeDate = orderRepository.findAllByCreationDateTimeBefore(expireBeforeDate);

        List<Order> expiredOrders = allOrdersBeforeDate.stream().filter(isExpired()).collect(Collectors.toList());

        expiredOrders.forEach(o -> orderService.expireOrder(o));
    }

    @Scheduled(fixedRate = USER_CLEANUP_CHECK_INTERVAL_MINUTES * 60 * 1000)
    public void CleanUpUsers() {
        LocalDateTime now = LocalDateTime.now();
        List<VerificationToken> allExpiredVerificationTokens = verificationTokenRepository.findAllByExpiryDateBefore(now);
        allExpiredVerificationTokens.forEach(t -> handleExpiredVerificationToken(t));
    }

    public static Predicate<Order> isExpired() {
        return o -> o.getStatus().equals(OrderStatus.CREATING) || o.getStatus().equals(OrderStatus.EXPIRED) ||
                o.getStatus().equals(OrderStatus.CANCELLED);
    }

    private void handleExpiredVerificationToken(VerificationToken verificationToken) {
        verificationTokenRepository.delete(verificationToken);
        userRepository.delete(verificationToken.getUser());
    }
}
