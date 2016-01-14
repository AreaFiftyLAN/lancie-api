package ch.wisv.areafiftylan.dto;

import ch.wisv.areafiftylan.model.util.TicketOptions;
import lombok.Getter;

/**
 * Created by sille on 10-1-16.
 */
public class TicketInformation {
    @Getter
    private int limit;

    @Getter
    private int sold;

    @Getter
    private double price;

    @Getter
    private double discountPrice;

    public TicketInformation(int limit, int sold, double price) {
        this.limit = limit;
        this.sold = sold;
        this.price = price;
        this.discountPrice = price + TicketOptions.CHMEMBER.getPrice();
    }
}
