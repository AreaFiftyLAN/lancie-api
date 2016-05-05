package ch.wisv.areafiftylan.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * Created by beer on 5-5-16.
 */
public class RFIDLinkDTO {
    @NotNull @Getter @Setter
    private String rfid;

    @NotNull @Getter @Setter
    private Long ticketId;
}
