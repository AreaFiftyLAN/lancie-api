package ch.wisv.areafiftylan.web.tournament.model;

import ch.wisv.areafiftylan.web.sponsor.model.Sponsor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.logstash.logback.encoder.org.apache.commons.lang.builder.EqualsBuilder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Tournament {

    @Id
    @GeneratedValue
    private Long id;

    private TournamentType type;

    private String buttonTitle;
    private String buttonImagePath;
    private String format;

    private String headerTitle;
    private String description;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "tournament_prize")
    private List<String> prizes = new ArrayList<>();

    @ManyToOne(targetEntity = Sponsor.class)
    @JsonIgnoreProperties("tournaments")
    private Sponsor sponsor;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tournament that = (Tournament) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (type != that.type) return false;
        if (buttonTitle != null ? !buttonTitle.equals(that.buttonTitle) : that.buttonTitle != null) return false;
        if (buttonImagePath != null ? !buttonImagePath.equals(that.buttonImagePath) : that.buttonImagePath != null)
            return false;
        if (format != null ? !format.equals(that.format) : that.format != null) return false;
        if (headerTitle != null ? !headerTitle.equals(that.headerTitle) : that.headerTitle != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (prizes != null && that.prizes != null) {
            for (int i = 0; i < prizes.size(); i++) {
                if (!prizes.get(i).equals(that.prizes.get(i))) return false;
            }
        } else {
            if (prizes == null ^ that.prizes == null) return false;
        }
        return sponsor != null ? sponsor.equals(that.sponsor) : that.sponsor == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (buttonTitle != null ? buttonTitle.hashCode() : 0);
        result = 31 * result + (buttonImagePath != null ? buttonImagePath.hashCode() : 0);
        result = 31 * result + (format != null ? format.hashCode() : 0);
        result = 31 * result + (headerTitle != null ? headerTitle.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (prizes != null ? prizes.hashCode() : 0);
        result = 31 * result + (sponsor != null ? sponsor.hashCode() : 0);
        return result;
    }
}
