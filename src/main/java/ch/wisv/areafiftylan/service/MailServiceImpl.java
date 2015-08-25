package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.MailDTO;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;


    @Override
    public void sendMail(String recipient, String sender, String subject, String message) {

    }

    @Override
    public void sendTemplateMailToTeam(Team team, MailDTO mailDTO) {

    }

    @Override
    public void sendTemplateMailToAll(MailDTO mailDTO) {

    }

    @Override
    public void sendTemplateMailToUser(User user, MailDTO mailDTO) {

    }

    public void sendMailWithInline(
            final String recipientName, final String recipientEmail, final String imageResourceName,
            final byte[] imageBytes, final String imageContentType, final Locale locale)
            throws MessagingException {

        // Prepare the evaluation context
        final Context ctx = new Context(locale);
        ctx.setVariable("name", recipientName);
        ctx.setVariable("subscriptionDate", new Date());
        ctx.setVariable("hobbies", Arrays.asList("Cinema", "Sports", "Music"));

        // Prepare message using a Spring helper
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message =
                new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart
        message.setSubject("Example HTML email with inline image");
        message.setFrom("thymeleaf@example.com");
        message.setTo(recipientEmail);

        // Create the HTML body using Thymeleaf
        final String htmlContent = this.templateEngine.process("email-inlineimage.html", ctx);
        message.setText(htmlContent, true); // true = isHtml

        // Send mail
        this.mailSender.send(mimeMessage);

    }

}
