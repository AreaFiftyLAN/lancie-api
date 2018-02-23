package ch.wisv.areafiftylan.notifications;

import in.ashwanthkumar.slack.webhook.Slack;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SlackNotificationService {

    @After("execution(* ch.wisv.areafiftylan.utils.TestDataRunner.run (..))")
    public void sendSlackMessage() {
        Slack slackApi = new Slack("https://hooks.slack.com/services/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");
        System.out.println("Normally would have sent a slack message now");
        /*
        try {
            slackApi.sendToChannel("#lanciedev").push(new SlackMessage("Message after testrunner run() method"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
}
