package ch.wisv.areafiftylan.utils.mail.template;

import ch.wisv.areafiftylan.exception.MailTemplateNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;

@Service
public class MailTemplateServiceJSONImpl implements MailTemplateService {

    /**
     * Reads a JSON file and casts it to a MailTemplate.
     * This method assumes correctness of the JSON file.
     *
     * @param templateName The name of the JSON file.
     * @return MailTemplate if successful, null otherwise.
     */
    @Override
    public MailTemplate getMailTemplateByName(String templateName) {
        MailTemplate mailTemplate;
        try {
            Object object = new ObjectMapper().readValue(new FileReader("config/mail/" + templateName + ".json"), Object.class);
            mailTemplate = (MailTemplate) object;
        } catch (IOException e) {
            throw new MailTemplateNotFoundException(templateName);
        }
        return mailTemplate;
    }
}
