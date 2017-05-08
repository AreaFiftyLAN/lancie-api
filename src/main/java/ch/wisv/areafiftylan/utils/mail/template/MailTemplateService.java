package ch.wisv.areafiftylan.utils.mail.template;

import java.util.Collection;

public interface MailTemplateService {

    MailTemplate addMailTemplate(String templateName, String subject, String message);

    MailTemplate updateMailTemplate(Long id, MailTemplateDTO mailTemplateDTO);

    Collection<MailTemplate> getAllMailTemplates();

    MailTemplate getMailTemplateById(Long id);

    MailTemplate getMailTemplateByTemplateName(String templateName);

    void deleteMailTemplateById(Long id);

    MailTemplate deleteMailTemplateByTemplateName(String templateName);
}
