package ch.wisv.areafiftylan.dto;

import ch.wisv.areafiftylan.model.util.TicketType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * Created by sille on 22-12-15.
 */
public class TicketDTO {
    @NotNull @Getter @Setter
    TicketType type;

    @NotNull @Setter
    Boolean pickupService;

    public boolean hasPickupService() {
        return pickupService;
    }

    @NotNull @Setter
    Boolean chMember;

    public Boolean isCHMember() {
        return chMember;
    }
}
