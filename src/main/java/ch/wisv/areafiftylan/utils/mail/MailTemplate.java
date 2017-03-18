package ch.wisv.areafiftylan.utils.mail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

@NoArgsConstructor
@AllArgsConstructor
public class MailTemplate {

    @Getter
    @Setter
    @NotEmpty
    String subject;

    @Getter
    @Setter
    @NotEmpty
    String message;
}
