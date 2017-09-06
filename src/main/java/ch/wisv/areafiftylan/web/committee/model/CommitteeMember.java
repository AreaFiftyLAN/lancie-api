package ch.wisv.areafiftylan.web.committee.model;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
public class CommitteeMember {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private Long position;

    @NonNull
    private String name;

    @NonNull
    private String function;

    @NonNull
    private String icon;
}
