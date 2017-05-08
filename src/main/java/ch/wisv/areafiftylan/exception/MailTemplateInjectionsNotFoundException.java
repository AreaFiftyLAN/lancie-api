package ch.wisv.areafiftylan.exception;

public class MailTemplateInjectionsNotFoundException extends RuntimeException {

    public MailTemplateInjectionsNotFoundException(String templateName) {
        super("Mail template injections of template " + templateName + " not found.");
    }
}
