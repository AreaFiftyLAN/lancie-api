package ch.wisv.areafiftylan.dto;

import org.hibernate.validator.constraints.NotEmpty;

public class ProfileDTO {

    @NotEmpty
    private String firstName = "";

    @NotEmpty
    private String lastName = "";

    private String displayName = "";

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
