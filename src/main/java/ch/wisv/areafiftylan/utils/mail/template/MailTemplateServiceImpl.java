package ch.wisv.areafiftylan.utils.mail.template;

import ch.wisv.areafiftylan.exception.MailTemplateInjectionsNotFoundException;
import ch.wisv.areafiftylan.exception.MailTemplateNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MailTemplateServiceImpl implements MailTemplateService {

    private final MailTemplateRepository templateRepository;
    private final MailTemplateInjectionsRepository injectionsRepository;

    public MailTemplateServiceImpl(MailTemplateRepository templateRepository, MailTemplateInjectionsRepository injectionsRepository) {
        this.templateRepository = templateRepository;
        this.injectionsRepository = injectionsRepository;
    }

    @Override
    public MailTemplate addMailTemplate(String templateName, String subject, String message) {
        MailTemplate mailTemplate = new MailTemplate();
        mailTemplate.setTemplateName(templateName);
        mailTemplate.setSubject(subject);
        mailTemplate.setMessage(message);
        return templateRepository.save(mailTemplate);
    }

    @Override
    public MailTemplate getMailTemplateById(Long id) {
        return templateRepository.findOne(id);
    }

    @Override
    public MailTemplate getMailTemplateByTemplateName(String templateName) {
        return templateRepository.findOneByTemplateName(templateName)
                .orElseThrow(() -> new MailTemplateNotFoundException(templateName));
    }

    @Override
    public void deleteMailTemplateById(Long id) {
        templateRepository.delete(id);
    }

    @Override
    public MailTemplate deleteMailTemplateByTemplateName(String templateName) {
        MailTemplate mailTemplate = getMailTemplateByTemplateName(templateName);
        templateRepository.delete(mailTemplate);
        return mailTemplate;
    }

    @Override
    public MailTemplateInjections addMailTemplateInjections(String templateName, Map<String, String> injections) {
        MailTemplateInjections mailTemplateInjections = new MailTemplateInjections();
        mailTemplateInjections.setTemplateName(templateName);
        mailTemplateInjections.setInjections(injections);
        return injectionsRepository.save(mailTemplateInjections);
    }

    @Override
    public MailTemplateInjections getMailTemplateInjectinosById(Long id) {
        return injectionsRepository.findOne(id);
    }

    @Override
    public MailTemplateInjections getMailTemplateInjectionsByTemplateName(String templateName) {
        return injectionsRepository.findOneByTemplateName(templateName)
                .orElseThrow(() -> new MailTemplateInjectionsNotFoundException(templateName));
    }

    @Override
    public void deleteMailTemplateInjectionsById(Long id) {
        injectionsRepository.delete(id);
    }

    @Override
    public MailTemplateInjections deleteMailTemplateInjectionsByTemplateName(String templateName) {
        MailTemplateInjections injections = getMailTemplateInjectionsByTemplateName(templateName);
        injectionsRepository.delete(injections);
        return injections;
    }
}
