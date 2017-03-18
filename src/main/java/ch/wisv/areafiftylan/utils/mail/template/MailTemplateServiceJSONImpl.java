package ch.wisv.areafiftylan.utils.mail.template;

import ch.wisv.areafiftylan.exception.MailTemplateNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileReader;
import java.io.IOException;

//@Service
public class MailTemplateServiceJSONImpl implements MailTemplateService {

    @Override
    public MailTemplate addMailTemplate(String templateName, String subject, String message) {
        return null;
    }

    @Override
    public MailTemplate getMailTemplateById(Long id) {
        return null;
    }

    /**
     * Reads a JSON file and casts it to a MailTemplate.
     * This method assumes correctness of the JSON file.
     *
     * @param templateName The name of the JSON file.
     * @return MailTemplate if successful, null otherwise.
     */
    @Override
    public MailTemplate getMailTemplateByTemplateName(String templateName) {
        MailTemplate mailTemplate;
        try {
            Object object = new ObjectMapper().readValue(new FileReader("config/mail/" + templateName + ".json"), Object.class);
            mailTemplate = (MailTemplate) object;
        } catch (IOException e) {
            throw new MailTemplateNotFoundException(templateName);
        }
        return mailTemplate;
    }

    @Override
    public void deleteMailTemplateById(Long id) {

    }

    @Override
    public MailTemplate deleteMailTemplateByTemplateName(String templateName) {
        return null;
    }
}
