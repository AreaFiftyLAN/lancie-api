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

package ch.wisv.areafiftylan.utils.mail;

import ch.wisv.areafiftylan.products.model.order.Order;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.users.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.Locale;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    private final SpringTemplateEngine templateEngine;

    @Value("${a5l.mail.sender}")
    String sender;

    @Value("${a5l.mail.contact}")
    String contact;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    private void sendMailWithContent(String recipientEmail, String subject, String content) {
        // Prepare message using a Spring helper
        MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        MimeMessageHelper message;

        try {
            message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            message.setSubject("[Area FiftyLAN] " + subject);
            message.setFrom(sender);
            message.setTo(recipientEmail);

            // Create the HTML body using Thymeleaf
            message.setText(content, true); // true = isHtml

            // Send mail
            this.mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new MailPreparationException("Unable to prepare email", e.getCause());
        } catch (MailException m) {
            throw new MailSendException("Unable to send email", m.getCause());
        }
    }

    @Override
    public void sendMail(String recipientEmail, String recipientName, String subject, String messageString) {
        String htmlContent = prepareHtmlContent(recipientName, messageString);
        sendMailWithContent(recipientEmail, subject, htmlContent);
    }

    @Override
    public void sendContactMail(String senderEmail, String subject, String messageString) {
        MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        MimeMessageHelper message;

        try {
            message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            message.setSubject("[Contact] " + subject);
            message.setFrom(senderEmail);
            message.setTo(contact);

            message.setText(messageString);

            this.mailSender.send(mimeMessage);
        } catch (MessagingException m) {
            throw new MailSendException("Unable to send email", m.getCause());
        }
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
        for (User user : team.getMembers()) {
            sendTemplateMailToUser(user, mailDTO);
        }
    }

    @Override
    public void sendTemplateMailToAll(Collection<User> users, MailDTO mailDTO) {
        for (User user : users) {
            sendTemplateMailToUser(user, mailDTO);
        }
    }

    @Override
    public void sendTemplateMailToUser(User user, MailDTO mailDTO) {
        sendMail(user.getUsername(), user.getProfile().getFirstName(), mailDTO.getSubject(), mailDTO.getMessage());
    }

    @Override
    public void sendVerificationmail(User user, String url) {
        String message =
                "Please click on the following link to complete your registration: <a href=\"" + url + "\">" + url +
                        "</a><br /><br />If the link does not work, please copy the link and" +
                        " paste it into your browser.";
        sendMail(user.getUsername(), user.getUsername(), "Confirm your registration", message);
    }

    @Override
    public void sendOrderConfirmation(Order order) {
        // Prepare the evaluation context
        final Context ctx = new Context(new Locale("en"));
        ctx.setVariable("name",
                order.getUser().getProfile().getFirstName() + " " + order.getUser().getProfile().getLastName());
        ctx.setVariable("order", order);
        String content = this.templateEngine.process("orderConfirmation", ctx);

        sendMailWithContent(order.getUser().getUsername(), "Order Confirmation", content);
    }

    @Override
    public void sendPasswordResetMail(User user, String url) {
        String message = "Please click on the following link to reset your password: <a href=\"" + url + "\">" + url +
                "</a><br /><br />If the link does not work, please copy the link and" + " paste it into your browser.";
        sendMail(user.getUsername(), user.getUsername(), "Password reset requested", message);
    }

    @Override
    public void sendTeamInviteMail(User user, String teamName, User teamCaptain) {

        String message =
                "You've been invited to join \"Team " + teamName + "\" by " + teamCaptain.getProfile().getFirstName() +
                        " " + teamCaptain.getProfile().getLastName() +
                        "! Please log in to My Area to accept the invitation.";

        sendMail(user.getUsername(), user.getUsername(), "You've been invited to \"Team " + teamName + "\"", message);
    }

    @Override
    public void sendSeatOverrideMail(User user) {
        String subject = "Your seat was reset";
        String message = "Your seat was reset by an admin.\nPlease log in to My Area to select your new seat.";
        sendMail(user.getUsername(), user.getUsername(), subject, message);
    }

    @Override
    public void sendTicketTransferMail(User sender, User receiver, String url) {
        String message = sender.getProfile().firstName +
                " has sent you a ticket for AreaFiftyLAN! To accept this ticket please click on the following link: " +
                "<a href=\"" + url + "\">" + url + "</a>";
        sendMail(receiver.getUsername(),
                receiver.getProfile().getFirstName() + " " + receiver.getProfile().getLastName(),
                "A ticket for AreaFiftyLAN has been sent to you!", message);
    }
}
