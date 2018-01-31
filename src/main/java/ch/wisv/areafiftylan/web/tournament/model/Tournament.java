package ch.wisv.areafiftylan.web.tournament.model;

import ch.wisv.areafiftylan.web.sponsor.model.Sponsor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
