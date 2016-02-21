package ch.wisv.areafiftylan.model.util;

/**
 * Created by sille on 28-12-15.
 */
public enum TicketOptions {
    PICKUPSERVICE(2.50F), CHMEMBER(-5.00F);

    float price;

    TicketOptions(float price) {
        this.price = price;
    }

    public float getPrice() {
        return price;
    }
}
