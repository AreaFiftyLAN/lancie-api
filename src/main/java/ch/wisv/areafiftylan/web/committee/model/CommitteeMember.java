package ch.wisv.areafiftylan.web.committee.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class CommitteeMember {

    @Id
    private Long position;

    private String name;

    private String function;

    private String icon;
}
