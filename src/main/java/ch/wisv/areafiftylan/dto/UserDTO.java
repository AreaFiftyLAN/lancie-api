package ch.wisv.areafiftylan.dto;

import ch.wisv.areafiftylan.model.util.Role;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

public class UserDTO {

    @NotEmpty @Getter @Setter
    private String username = "";

    @NotEmpty @Getter @Setter
    private String email = "";

    @NotEmpty @Getter @Setter
    private String password = "";

    @NotNull @Getter @Setter
    private Role role = Role.ROLE_USER;
}
