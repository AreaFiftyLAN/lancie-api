package ch.wisv.areafiftylan.exception;

public class MailTemplateNotFoundException extends RuntimeException {

    public MailTemplateNotFoundException(String templateName) {
        super("Mail template " + templateName + " not found.");
    }
}
