package ch.wisv.areafiftylan.users.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class RoleDTO {

    @NotNull
    private Role role;
}