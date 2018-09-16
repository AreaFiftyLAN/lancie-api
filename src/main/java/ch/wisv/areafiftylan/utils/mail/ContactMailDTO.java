package ch.wisv.areafiftylan.utils.mail;

import lombok.Getter;
import javax.validation.constraints.NotEmpty;

public class ContactMailDTO extends MailDTO {
    @Getter
    @NotEmpty
    String sender;
}
