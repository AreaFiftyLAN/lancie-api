package ch.wisv.areafiftylan.utils;

import ch.wisv.areafiftylan.exception.SlackNotificationException;
import ch.wisv.areafiftylan.products.model.order.Order;
import ch.wisv.areafiftylan.products.model.order.OrderStatus;
import in.ashwanthkumar.slack.webhook.Slack;
import in.ashwanthkumar.slack.webhook.SlackMessage;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Aspect
@Component
@Profile("!development")
public class SlackNotificationAspect {

    private Slack slackApi;

    public SlackNotificationAspect(@Value("${slack}") String webHookUrl) {
        slackApi = new Slack(webHookUrl);
    }

    private void sendSlackMessage(String message) {
        try {
            slackApi.sendToChannel("#lanciedev").push(new SlackMessage(message));
        } catch (IOException e) {
            throw new SlackNotificationException(e);
        }
    }

    @AfterReturning(pointcut = "execution(* ch.wisv.areafiftylan.products.service.MolliePaymentService.updateStatus(..))", returning = "order")
    public void notifyTicketSale(Order order) {
        if (order.getStatus() == OrderStatus.PAID) {
            sendSlackMessage(order.getTickets().size() + " tickets bought! :tada:");
        }
    }

    @After("execution(* ch.wisv.areafiftylan.products.service.TicketServiceImpl.transferTicket(..))")
    public void notifyTicketTransfer() {
        sendSlackMessage("A ticket transfer has been performed");
    }

    @AfterThrowing(pointcut = "execution(* ch.wisv.areafiftylan.exception.*.*(..)) && !execution(* ch.wisv.areafiftylan.exception.SlackNotificationException.*(..))", throwing = "ex")
    public void forwardException(Throwable ex) {
        sendSlackMessage("An exception has been thrown: " + ex.getMessage());
    }


}
