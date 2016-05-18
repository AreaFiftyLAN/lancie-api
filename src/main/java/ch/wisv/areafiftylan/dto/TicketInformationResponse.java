package ch.wisv.areafiftylan.dto;

import ch.wisv.areafiftylan.model.util.TicketOptions;
import ch.wisv.areafiftylan.model.util.TicketType;
import lombok.Getter;

/**
 * Created by sille on 10-1-16.
 */
public class TicketInformationResponse {
    @Getter
    private String ticketType;

    @Getter
    private int limit;

    @Getter
    private int numberSold;

    @Getter
    private double price;

    @Getter
    private String text;

    @Getter
    private double chMemberDiscountPrice;

    @Getter
    private double pickupServicePrice;

    @Getter
    private String deadline;

    public TicketInformationResponse(TicketType type, int numberSold) {
        this.ticketType = type.name();
        this.limit = type.getLimit();
        this.numberSold = numberSold;
        this.price = type.getPrice();
        this.text = type.getText();
        this.chMemberDiscountPrice = TicketOptions.CHMEMBER.getPrice();
        this.pickupServicePrice = TicketOptions.PICKUPSERVICE.getPrice();
        this.deadline = type.getDeadline().toString();
    }
}
