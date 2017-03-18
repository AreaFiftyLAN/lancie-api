package ch.wisv.areafiftylan.utils.mail.template.injections;

import ch.wisv.areafiftylan.exception.MailTemplateInjectionsNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MailTemplateInjectionsServiceImpl implements MailTemplateInjectionsService {

    private final MailTemplateInjectionsRepository injectionsRepository;

    public MailTemplateInjectionsServiceImpl(MailTemplateInjectionsRepository injectionsRepository) {
        this.injectionsRepository = injectionsRepository;
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
