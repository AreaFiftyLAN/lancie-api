package ch.wisv.areafiftylan.web.sponsor.model;

import ch.wisv.areafiftylan.web.tournament.model.Tournament;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class Sponsor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String imageName;

    private String website;

    private SponsorType type;

    @OneToMany(mappedBy = "sponsor", fetch = FetchType.EAGER)
    private Set<Tournament> tournaments;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sponsor sponsor = (Sponsor) o;

        if (!id.equals(sponsor.id)) return false;
        if (name != null ? !name.equals(sponsor.name) : sponsor.name != null) return false;
        if (imageName != null ? !imageName.equals(sponsor.imageName) : sponsor.imageName != null) return false;
        if (website != null ? !website.equals(sponsor.website) : sponsor.website != null) return false;
        return type == sponsor.type;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (imageName != null ? imageName.hashCode() : 0);
        result = 31 * result + (website != null ? website.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
