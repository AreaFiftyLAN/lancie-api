package ch.wisv.areafiftylan.unit;

import ch.wisv.areafiftylan.products.model.order.Order;
import ch.wisv.areafiftylan.security.authentication.AuthenticationServiceImpl;
import ch.wisv.areafiftylan.utils.ScheduledTasks;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.Optional;

@Import({ScheduledTasks.class, AuthenticationServiceImpl.class})
public class TaskSchedulerTest extends ServiceTest {

    @Autowired
    ScheduledTasks scheduledTasks;

    @Test
    public void expireOrders() {
        Order order = new Order();
        order.setCreationDateTime(LocalDateTime.now().minusMinutes(20));

        order = orderRepository.saveAndFlush(order);

        scheduledTasks.ExpireOrders();

        Optional<Order> expiredOrder = orderRepository.findById(order.getId());
        Assert.assertTrue(expiredOrder.isEmpty());
    }

    @Test
    public void expireOrderWithTicket() {
        Order order = new Order();
        order.setCreationDateTime(LocalDateTime.now().minusMinutes(20));
        order.addTicket(persistTicket());

        order = orderRepository.saveAndFlush(order);

        scheduledTasks.ExpireOrders();

        Optional<Order> expiredOrder = orderRepository.findById(order.getId());
        Assert.assertTrue(expiredOrder.isEmpty());
    }
}