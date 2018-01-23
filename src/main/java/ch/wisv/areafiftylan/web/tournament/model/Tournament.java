package ch.wisv.areafiftylan.web.tournament.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import ch.wisv.areafiftylan.web.sponsor.model.Sponsor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
