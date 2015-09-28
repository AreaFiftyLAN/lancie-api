package ch.wisv.areafiftylan.dto;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

public class TeamDTO {

    @NotEmpty
    private String teamName;

    @NotNull
    private Long captainID;

    public String getTeamName() {
        return teamName;
    }

    public Long getCaptainID() {
        return captainID;
    }

}
