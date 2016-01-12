package ch.wisv.areafiftylan.dto;

import ch.wisv.areafiftylan.model.util.TicketOptions;
import ch.wisv.areafiftylan.model.util.TicketType;
import lombok.Getter;

/**
 * Created by sille on 10-1-16.
 */
public class TicketInformationResponse {
    @Getter
    private TicketType ticketType;

    @Getter
    private int limit;

    @Getter
    private int numberSold;

    @Getter
    private double price;

    @Getter
    private double discountPrice;

    public TicketInformationResponse(TicketType type, int numberSold) {
        this.ticketType = type;
        this.limit = type.getLimit();
        this.numberSold = numberSold;
        this.price = type.getPrice();
        this.discountPrice = type.getPrice() + TicketOptions.CHMEMBER.getPrice();
    }
}
