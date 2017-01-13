package ch.wisv.areafiftylan.web.sponsor.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@NoArgsConstructor
public class Sponsor {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String imageName;

    @Getter
    @Setter
    private String website;

    @Getter
    @Setter
    private SponsorType type;

    public Sponsor(String name, String imageName, String website, SponsorType type) {
        this.name = name;
        this.imageName = imageName;
        this.website = website;
        this.type = type;
    }
}
