package ch.wisv.areafiftylan.dto;

import ch.wisv.areafiftylan.model.Gender;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

public class ProfileDTO {

    @NotNull
    public Gender gender;
    @NotEmpty
    public String address = "";
    @NotEmpty
    public String zipcode = "";
    @NotEmpty
    public String city = "";
    @NotEmpty
    public String phoneNumber = "";
    public String notes = "";
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

    public Gender getGender() {
        return gender;
    }

    public String getAddress() {
        return address;
    }

    public String getZipcode() {
        return zipcode;
    }

    public String getCity() {
        return city;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getNotes() {
        return notes;
    }
}
