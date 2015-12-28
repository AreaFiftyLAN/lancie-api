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

    @NotNull
    Boolean chMember;

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

    public Boolean isCHMember() {
        return chMember;
    }

    public void setChMember(Boolean chMember) {
        this.chMember = chMember;
    }
}
