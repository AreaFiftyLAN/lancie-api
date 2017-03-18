package ch.wisv.areafiftylan.utils.mail.template;

public interface MailTemplateService {

    MailTemplate addMailTemplate(String templateName, String subject, String message);

    MailTemplate getMailTemplateById(Long id);

    MailTemplate getMailTemplateByTemplateName(String templateName);

    void deleteMailTemplateById(Long id);

    MailTemplate deleteMailTemplateByTemplateName(String templateName);
}
