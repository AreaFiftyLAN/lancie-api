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
import java.util.Collection;
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
    public void sendTemplateMailToTeam(Team team, MailDTO mailDTO) throws MessagingException {
        for (User user : team.getMembers()) {
            sendTemplateMailToUser(user, mailDTO);
        }
    }

    @Override
    public void sendTemplateMailToAll(Collection<User> users, MailDTO mailDTO) throws MessagingException {
        for (User user : users) {
            sendTemplateMailToUser(user, mailDTO);
        }
    }

    @Override
    public void sendTemplateMailToUser(User user, MailDTO mailDTO) throws MessagingException {
        sendMail(user.getEmail(), user.getProfile().getFirstName(), "lancie@ch.tudelft.nl", mailDTO.getSubject(),
                mailDTO.getMessage());
    }

    @Override
    public void sendVerificationmail(User user, String url) throws MessagingException {
        String message = "Please click on the following link to complete your registration: " + url;
        sendMail(user.getEmail(), user.getUsername(), null, "Confirm your registration", message);
    }

    @Override
    public void sendPasswordResetMail(User user, String url) throws MessagingException {
        String message = "Please click on the following link to reset your password: " + url;
        sendMail(user.getEmail(), user.getUsername(), null, "Password reset requested", message);
    }
}
