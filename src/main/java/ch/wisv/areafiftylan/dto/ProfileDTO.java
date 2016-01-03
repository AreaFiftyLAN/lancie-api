package ch.wisv.areafiftylan.dto;

import ch.wisv.areafiftylan.model.util.Gender;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

public class ProfileDTO {

    @NotNull @Getter @Setter
    private Gender gender;

    @NotEmpty @Getter @Setter
    private String address = "";

    @NotEmpty @Getter @Setter
    private String zipcode = "";

    @NotEmpty @Getter @Setter
    private String city = "";

    @NotEmpty @Getter @Setter
    private String phoneNumber = "";

    @Getter @Setter
    private String notes = "";

    @NotEmpty @Getter @Setter
    private String firstName = "";

    @NotEmpty @Getter @Setter
    private String lastName = "";

    @Getter @Setter
    private String displayName = "";
}
