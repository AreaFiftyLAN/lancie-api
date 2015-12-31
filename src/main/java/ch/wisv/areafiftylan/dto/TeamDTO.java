package ch.wisv.areafiftylan.dto;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

public class TeamDTO {

    @NotEmpty
    private String teamName = "";

    @NotEmpty
    private String captainUsername = "";

    public String getTeamName() {
        return teamName;
    }

    public String getCaptainUsername() {
        return captainUsername;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public void setCaptainUsername(String captainUsername) {
        this.captainUsername = captainUsername;
    }
}
