package ch.wisv.areafiftylan.utils.mail.template;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
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
    @ElementCollection
    private Map<String, String> injections;
}
