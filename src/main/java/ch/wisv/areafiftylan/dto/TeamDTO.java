package ch.wisv.areafiftylan.dto;

import ch.wisv.areafiftylan.model.User;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Collection;

public class TeamDTO {

    @NotEmpty
    private String teamName;

    @NotEmpty
    private Long captianID;

    public String getTeamName() {
        return teamName;
    }

    public Long getCaptianID() {
        return captianID;
    }

}
