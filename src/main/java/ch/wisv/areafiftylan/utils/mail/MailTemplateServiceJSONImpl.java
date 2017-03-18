package ch.wisv.areafiftylan.utils.mail;

import ch.wisv.areafiftylan.exception.MailTemplateNotFoundException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class MailTemplateServiceJSONImpl implements MailTemplateService {

    @Override
    public MailTemplate getMailTemplateByName(String templateName) {
        MailTemplate mailTemplate;
        JSONParser jsonParser = new JSONParser();
        try {
            Object object = jsonParser.parse(new FileReader("config/mail/" + templateName + ".json"));
            JSONObject jsonObject = (JSONObject) object;
            mailTemplate = new MailTemplate();
            mailTemplate.setSubject((String) jsonObject.get("subject"));
            mailTemplate.setMessage((String) jsonObject.get("message"));
        } catch (IOException | ParseException e) {
            throw new MailTemplateNotFoundException(templateName);
        }
        return mailTemplate;
    }
}
