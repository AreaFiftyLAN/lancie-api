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

import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.model.order.Order;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.utils.mail.template.MailTemplate;
import ch.wisv.areafiftylan.utils.mail.template.MailTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final MailTemplateService templateService;
    private final SpringTemplateEngine templateEngine;

    @Value("${a5l.mail.sender}")
    String sender;

    @Value("${a5l.mail.contact}")
    String contact;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender, MailTemplateService mailTemplateService, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateService = mailTemplateService;
        this.templateEngine = templateEngine;
    }

    //region Helper methods
    private void sendMimeMail(String sender, String recipientEmail, String subject, String content) {
        MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        MimeMessageHelper message;

        try {
            message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            message.setFrom(sender);
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(content, true);

            this.mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new MailPreparationException("Unable to prepare email", e.getCause());
        } catch (MailException m) {
            throw new MailSendException("Unable to send email", m.getCause());
        }
    }

    /**
     * Replaces substrings in the message and subject of the MailTemplate.
     * Using a Map<K, V>, replace all K with V.
     * Regex is allowed in the K values.
     *
     * @param mailTemplate The MailTemplate to inject.
     * @param injections The Strings to inject.
     * @return The injected MailTemplate.
     */
    private MailTemplate injectMailTemplate(MailTemplate mailTemplate, Map<String, String> injections) {
        String message = mailTemplate.getMessage();
        String subject = mailTemplate.getSubject();
        for (Map.Entry<String, String> injection : injections.entrySet()) {
            message = message.replaceAll(injection.getKey(), injection.getValue());
            subject = subject.replaceAll(injection.getKey(), injection.getValue());
        }
        mailTemplate.setSubject(subject);
        mailTemplate.setMessage(message);
        return mailTemplate;
    }

    private String formatRecipient(User user) {
        if (user.getProfile() == null ||
            user.getProfile().getFirstName().equals("") ||
            user.getProfile().getLastName().equals("")) {
            return user.getUsername();
        } else {
            return user.getProfile().getFirstName() + " " + user.getProfile().getLastName();
        }
    }
    //endregion Helper methods
    //region Generic mail methods
    @Override
    public void sendTemplateMail(String recipientEmail, String templateName, Map<String, String> injections) {
        MailTemplate mailTemplate = templateService.getMailTemplateByTemplateName(templateName);
        mailTemplate = injectMailTemplate(mailTemplate, injections);
        sendMimeMail(sender, recipientEmail, mailTemplate.getSubject(), mailTemplate.getMessage());
    }

    @Override
    public void sendTemplateMailToCollection(Collection<String> recipientEmails, String templateName, Map<String, String> injections) {
        for (String recipientEmail : recipientEmails) {
            sendTemplateMail(recipientEmail, templateName, injections);
        }
    }

    @Override
    public void sendCustomMail(String recipient, MailDTO mailDTO) {
        sendMimeMail(sender, recipient, mailDTO.getSubject(), mailDTO.getMessage());
    }

    @Override
    public void sendCustomMailToCollection(Collection<String> recipientEmails, MailDTO mailDTO) {
        for (String recipientEmail : recipientEmails) {
            sendCustomMail(recipientEmail, mailDTO);
        }
    }
    //endregion Generic mail methods
    //region Specific mail methods
    @Override
    public void sendContactMail(String sender, String subject, String message) {
        sendMimeMail(sender, contact, "[Contact] " + subject, message);
    }

    @Override
    public void sendVerificationmail(User user, String url) {
        Map<String, String> injections = new HashMap<>();
        injections.put("${url}", url);
        injections.put("${addressee}", formatRecipient(user));
        sendTemplateMail(user.getUsername(), "verification", injections);
    }

    @Override
    public void sendOrderConfirmationMail(Order order) {
        Map<String, String> injections = new HashMap<>();
        injections.put("${addressee}", formatRecipient(order.getUser()));
        String ticketinjection = "";
        for (Ticket ticket : order.getTickets()) {
            //TODO
            ticketinjection = ticketinjection + "<BUNCH OF HTML AROUND A TICKET>" + ticket.getType().getText() + ticket.getEnabledOptions() + "MORE HTML CLOSING THE TICKET";
        }

        sendTemplateMail(order.getUser().getUsername(), "orderConfirmation", injections);
    }

    @Override
    public void sendPasswordResetMail(User recipient, String url) {
        Map<String, String> injections = new HashMap<>();
        injections.put("${addressee}", formatRecipient(recipient));
        injections.put("${url}", url);
        sendTemplateMail(recipient.getUsername(), "passwordReset", injections);
    }

    @Override
    public void sendTeamInviteMail(User recipient, Team team) {
        Map<String, String> injections = new HashMap<>();
        injections.put("${addressee}", formatRecipient(recipient));
        injections.put("${teamname}", team.getTeamName());
        injections.put("${teamcaptain}", formatRecipient(team.getCaptain()));
        sendTemplateMail(recipient.getUsername(), "teamInvite", injections);
    }

    @Override
    public void sendSeatOverrideMail(User recipient) {
        Map<String, String> injections = new HashMap<>();
        injections.put("${addressee}", formatRecipient(recipient));
        sendTemplateMail(recipient.getUsername(), "seatOverride", injections);
    }

    @Override
    public void sendTicketTransferMail(User recipient, User sender, String url) {
        Map<String, String> injections = new HashMap<>();
        injections.put("${addressee}", formatRecipient(recipient));
        injections.put("${sender}", formatRecipient(sender));
        injections.put("${url}", url);
        sendTemplateMail(recipient.getUsername(), "seatOverride", injections);
    }
    //endregion Specific mail methods
}
