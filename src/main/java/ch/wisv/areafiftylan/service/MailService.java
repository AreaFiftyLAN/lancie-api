package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.MailDTO;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;

public interface MailService {

    void sendMail(String recipient, String sender, String subject, String message);

    void sendTemplateMailToTeam(Team team, MailDTO mailDTO);

    void sendTemplateMailToAll(MailDTO mailDTO);

    void sendTemplateMailToUser(User user, MailDTO mailDTO);
}
