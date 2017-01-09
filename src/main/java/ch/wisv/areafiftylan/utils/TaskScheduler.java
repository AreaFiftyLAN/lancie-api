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

package ch.wisv.areafiftylan.utils;

import ch.wisv.areafiftylan.products.model.order.Order;
import ch.wisv.areafiftylan.products.model.order.OrderStatus;
import ch.wisv.areafiftylan.products.service.OrderService;
import ch.wisv.areafiftylan.products.service.repository.OrderRepository;
import ch.wisv.areafiftylan.security.token.Token;
import ch.wisv.areafiftylan.security.token.VerificationToken;
import ch.wisv.areafiftylan.security.token.repository.VerificationTokenRepository;
import ch.wisv.areafiftylan.users.service.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    @Value("${a5l.orderKeepAlive:15}")
    private int ORDER_STAY_ALIVE_MINUTES;
    private final int ORDER_EXPIRY_CHECK_INTERVAL_SECONDS = 60;

    private final int USER_CLEANUP_CHECK_INTERVAL_MINUTES = 60;

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    @Autowired
    public TaskScheduler(OrderRepository orderRepository, OrderService orderService,
                         VerificationTokenRepository verificationTokenRepository, UserRepository userRepository) {
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

        expiredOrders.forEach(orderService::expireOrder);
    }

    @Scheduled(fixedRate = USER_CLEANUP_CHECK_INTERVAL_MINUTES * 60 * 1000)
    public void CleanUpUsers() {
        LocalDateTime now = LocalDateTime.now();
        List<VerificationToken> allExpiredVerificationTokens =
                verificationTokenRepository.findAllByExpiryDateBefore(now);
        allExpiredVerificationTokens.stream().filter(Token::isUnused).forEach(this::handleExpiredVerificationToken);
    }

    private static Predicate<Order> isExpired() {
        return o -> o.getStatus().equals(OrderStatus.ANONYMOUS) || o.getStatus().equals(OrderStatus.ASSIGNED) ||
                o.getStatus().equals(OrderStatus.EXPIRED) || o.getStatus().equals(OrderStatus.CANCELLED);
    }

    private void handleExpiredVerificationToken(VerificationToken verificationToken) {
        verificationTokenRepository.delete(verificationToken);
        userRepository.delete(verificationToken.getUser());
    }
}
