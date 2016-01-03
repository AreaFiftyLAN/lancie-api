package ch.wisv.areafiftylan.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

public class TeamDTO {

    @NotEmpty @Getter @Setter
    private String teamName = "";

    @NotEmpty @Getter @Setter
    private String captainUsername = "";
}
