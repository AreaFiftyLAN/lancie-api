package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.dto.MailDTO;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {
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
}
