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
}
