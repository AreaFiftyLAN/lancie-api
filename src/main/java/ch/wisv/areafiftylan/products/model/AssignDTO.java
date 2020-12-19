package ch.wisv.areafiftylan.products.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AssignDTO {


    @NotNull
    Long userID;

    @NotNull
    String ticketType;
}
