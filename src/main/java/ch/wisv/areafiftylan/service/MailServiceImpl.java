package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.MailDTO;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Locale;

@Service
public class MailServiceImpl implements MailService {

    private JavaMailSender mailSender;

    private SpringTemplateEngine templateEngine;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }


    @Override
    public void sendMail(String recipientEmail, String recipientName, String senderEmail, String subject,
                         String messageString) throws MessagingException {

        // Prepare message using a Spring helper
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart

        message.setSubject(subject);
        message.setFrom("LANcie <lancie@ch.tudelft.nl>");
        message.setTo(recipientEmail);

        // Create the HTML body using Thymeleaf
        String htmlContent = prepareHtmlContent(recipientName, messageString);
        message.setText(htmlContent, true); // true = isHtml

        // Send mail
        this.mailSender.send(mimeMessage);
    }

    private String prepareHtmlContent(String name, String message) {
        // Prepare the evaluation context
        final Context ctx = new Context(new Locale("en"));
        ctx.setVariable("name", name);
        ctx.setVariable("message", message);
        return this.templateEngine.process("mailTemplate", ctx);

    }

    @Override
    public void sendTemplateMailToTeam(Team team, MailDTO mailDTO) {
        throw new NotYetImplementedException();

    }

    @Override
    public void sendTemplateMailToAll(MailDTO mailDTO) {
        throw new NotYetImplementedException();
    }

    @Override
    public void sendTemplateMailToUser(User user, MailDTO mailDTO) throws MessagingException {
        sendMail(user.getEmail(), user.getProfile().getFirstName(), "lancie@ch.tudelft.nl", mailDTO.getSubject(),
                mailDTO.getMessage());
    }
}
