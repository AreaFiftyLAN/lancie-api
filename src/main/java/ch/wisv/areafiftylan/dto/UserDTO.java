package ch.wisv.areafiftylan.dto;

import ch.wisv.areafiftylan.model.util.Role;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

public class UserDTO {

    @NotEmpty
    private String username = "";

    @Email
    private String email = "";

    @NotEmpty
    private String password = "";

    @NotNull
    private Role role = Role.ROLE_USER;

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}
