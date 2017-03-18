package ch.wisv.areafiftylan.utils.mail.template.injections;

import java.util.Map;

public interface MailTemplateInjectionsService {

    MailTemplateInjections addMailTemplateInjections(String templateName, Map<String, String> injections);

    MailTemplateInjections getMailTemplateInjectinosById(Long id);

    MailTemplateInjections getMailTemplateInjectionsByTemplateName(String templateName);

    void deleteMailTemplateInjectionsById(Long id);

    MailTemplateInjections deleteMailTemplateInjectionsByTemplateName(String templateName);
}
