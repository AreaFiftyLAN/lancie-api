package ch.wisv.areafiftylan.dto;

import ch.wisv.areafiftylan.model.util.TicketType;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by sille on 10-1-16.
 */
public class TicketInformationResponse {
    @Getter
    @Setter
    private TicketType ticketType;

    @Getter
    @Setter
    private int limit;

    @Getter
    @Setter
    private int numberSold;

    @Getter
    @Setter
    private double price;

    public TicketInformationResponse(TicketType type, int numberSold) {
        this.ticketType = type;
        this.limit = type.getLimit();
        this.numberSold = numberSold;
        this.price = type.getPrice();
    }
}
