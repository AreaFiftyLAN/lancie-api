package ch.wisv.areafiftylan.notifications;

import in.ashwanthkumar.slack.webhook.Slack;
import in.ashwanthkumar.slack.webhook.SlackMessage;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Aspect
@Component
public class SlackNotificationService {

    public void sendSlackMessage(String message) {
        Slack slackApi = new Slack("https://hooks.slack.com/services/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");
        try {
            slackApi.sendToChannel("#lanciedev").push(new SlackMessage(message));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
