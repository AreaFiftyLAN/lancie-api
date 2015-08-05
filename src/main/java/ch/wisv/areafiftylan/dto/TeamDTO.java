package ch.wisv.areafiftylan.dto;

import ch.wisv.areafiftylan.model.User;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Collection;

public class TeamDTO {

    @NotEmpty
    private String teamName;
    
    private Long captianID;
}
