package ch.wisv.areafiftylan.model.util;

public enum TicketType {
    EARLY_FULL(35.00F, 50), REGULAR_FULL(40.00F, 100), LATE_FULL(45.00F, 50);

    private final float price;
    private final int limit;

    TicketType(float price, int limit) {
        this.price = price;
        this.limit = limit;
    }

    public float getPrice() {
        return price;
    }

    public int getLimit() {
        return limit;
    }
}
