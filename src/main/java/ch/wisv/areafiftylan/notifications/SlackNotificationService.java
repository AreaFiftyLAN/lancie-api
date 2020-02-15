package ch.wisv.areafiftylan.notifications;

import ch.wisv.areafiftylan.exception.SlackNotificationException;
import in.ashwanthkumar.slack.webhook.Slack;
import in.ashwanthkumar.slack.webhook.SlackMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SlackNotificationService {
    @Value("${slack.slackWebHook.url}")
    private String webHookUrl;
    private Slack slackApi = new Slack(webHookUrl);

    public SlackNotificationService(){
    }

    public void sendSlackMessage(String message) {
        try {
            slackApi.sendToChannel("#lanciedev").push(new SlackMessage(message));
        } catch (IOException e) {
            throw new SlackNotificationException(e);
        }
    }
}
