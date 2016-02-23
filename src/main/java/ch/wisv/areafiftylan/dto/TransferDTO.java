package ch.wisv.areafiftylan.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * Created by beer on 7-1-16.
 */
public class TransferDTO {

    @NotNull @Getter @Setter
    String ticketKey;

    @NotNull @Getter @Setter
    String goalUsername;
}
