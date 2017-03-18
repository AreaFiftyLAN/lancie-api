package ch.wisv.areafiftylan.utils.mail;

import ch.wisv.areafiftylan.exception.MailTemplateNotFoundException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
        JSONParser jsonParser = new JSONParser();
        try {
            Object object = jsonParser.parse(new FileReader("config/mail/" + templateName + ".json"));
            mailTemplate = (MailTemplate) object;
        } catch (IOException | ParseException e) {
            throw new MailTemplateNotFoundException(templateName);
        }
        return mailTemplate;
    }
}
