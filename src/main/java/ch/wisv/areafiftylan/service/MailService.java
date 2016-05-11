package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.MailDTO;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;

import java.util.Collection;

public interface MailService {

    void sendMail(String recipientEmail, String recipientName, String subject, String message);

    void sendTemplateMailToTeam(Team team, MailDTO mailDTO);

    void sendTemplateMailToAll(Collection<User> users, MailDTO mailDTO);

    void sendTemplateMailToUser(User user, MailDTO mailDTO);

    void sendVerificationmail(User user, String url);

    void sendPasswordResetMail(User user, String url);

    void sendTicketTransferMail(User sender, User receiver, String url);

    void sendTeamInviteMail(User user, String teamName, User teamCaptain);
}
