package ch.wisv.areafiftylan.utils.mail.template;

import ch.wisv.areafiftylan.exception.MailTemplateNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class MailTemplateServiceImpl implements MailTemplateService {

    private final MailTemplateRepository templateRepository;

    public MailTemplateServiceImpl(MailTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
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
    public Collection<MailTemplate> getAllMailTemplates() {
        return templateRepository.findAll();
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
}
