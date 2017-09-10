package ch.wisv.areafiftylan.web.faq.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class FaqPair {

    @Id
    @GeneratedValue
    private Long id;

    private String question;

    private String answer;
}
