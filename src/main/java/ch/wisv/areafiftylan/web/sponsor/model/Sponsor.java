package ch.wisv.areafiftylan.web.sponsor.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Sponsor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NonNull
    private String name;

    @NonNull
    private String imageName;

    @NonNull
    private String website;

    @NonNull
    private SponsorType type;
}
