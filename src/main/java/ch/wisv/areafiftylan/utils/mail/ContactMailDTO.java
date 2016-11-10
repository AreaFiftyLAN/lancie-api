package ch.wisv.areafiftylan.utils.mail;

import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;

public class ContactMailDTO extends MailDTO {
    @Getter
    @NotEmpty
    String sender;
}
