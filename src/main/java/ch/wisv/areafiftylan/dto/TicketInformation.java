package ch.wisv.areafiftylan.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by sille on 10-1-16.
 */
public class TicketInformation {
    @Getter
    @Setter
    private int limit;

    @Getter
    @Setter
    private int sold;

    @Getter
    @Setter
    private double price;

    public TicketInformation(int limit, int sold, double price) {
        this.limit = limit;
        this.sold = sold;
        this.price = price;
    }
}
