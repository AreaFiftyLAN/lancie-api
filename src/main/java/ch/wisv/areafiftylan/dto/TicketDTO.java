package ch.wisv.areafiftylan.dto;

import ch.wisv.areafiftylan.model.util.TicketType;

/**
 * Created by sille on 22-12-15.
 */
public class TicketDTO {
    TicketType type;
    boolean pickupService;

    public TicketType getType() {
        return type;
    }

    public boolean hasPickupService() {
        return pickupService;
    }
}
