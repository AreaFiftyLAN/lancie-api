package ch.wisv.areafiftylan.utils.mail.template;

import java.util.Map;

public interface MailTemplateService {

    MailTemplate addMailTemplate(String templateName, String subject, String message);

    MailTemplate getMailTemplateById(Long id);

    MailTemplate getMailTemplateByTemplateName(String templateName);

    void deleteMailTemplateById(Long id);

    MailTemplate deleteMailTemplateByTemplateName(String templateName);

    MailTemplateInjections addMailTemplateInjections(String templateName, Map<String, String> injections);

    MailTemplateInjections getMailTemplateInjectinosById(Long id);

    MailTemplateInjections getMailTemplateInjectionsByTemplateName(String templateName);

    void deleteMailTemplateInjectionsById(Long id);

    MailTemplateInjections deleteMailTemplateInjectionsByTemplateName(String templateName);
}
