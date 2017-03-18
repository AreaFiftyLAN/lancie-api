package ch.wisv.areafiftylan.utils.mail.template;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@NoArgsConstructor
@Entity
public class MailTemplate {

    @Id
    @Getter
    @GeneratedValue
    private Long id;

    @Getter
    @Setter
    @NotEmpty
    private String templateName;

    @Getter
    @Setter
    @NotEmpty
    private String subject;

    @Getter
    @Setter
    @NotEmpty
    private String message;
}
