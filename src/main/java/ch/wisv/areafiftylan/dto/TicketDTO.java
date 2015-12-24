package ch.wisv.areafiftylan.dto;

import ch.wisv.areafiftylan.model.util.TicketType;

import javax.validation.constraints.NotNull;

/**
 * Created by sille on 22-12-15.
 */
public class TicketDTO {
    @NotNull
    TicketType type;

    @NotNull
    Boolean pickupService;

    public TicketType getType() {
        return type;
    }

    public boolean hasPickupService() {
        return pickupService;
    }

    public void setType(TicketType type) {
        this.type = type;
    }

    public void setPickupService(Boolean pickupService) {
        this.pickupService = pickupService;
    }
}
