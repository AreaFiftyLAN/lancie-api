package ch.wisv.areafiftylan.utils.mail.template;

import lombok.Data;

@Data
public class MailTemplateDTO {

    private String templateName;

    private String subject;

    private String message;
}
