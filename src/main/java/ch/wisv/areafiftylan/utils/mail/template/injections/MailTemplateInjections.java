package ch.wisv.areafiftylan.utils.mail.template.injections;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Entity
@NoArgsConstructor
public class MailTemplateInjections {

    @Id
    @Getter
    @GeneratedValue
    private Long id;

    @Getter
    @Setter
    @NotNull
    private String templateName;

    @Getter
    @Setter
    @NotNull
    private Map<String, String> injections;
}
