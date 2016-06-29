package ch.wisv.areafiftylan.model.util;

import java.time.LocalDateTime;

public enum TicketType {
    EARLY_FULL("Early Bird", 37.50F, 50, LocalDateTime.of(2016, 6, 3, 0, 0), true),
    REGULAR_FULL("Regular", 40.00F, 0, LocalDateTime.of(2016, 5, 28, 23, 59), true),
    LAST_MINUTE("Last Minute", 42.50F, 0, LocalDateTime.of(2016, 6, 5, 23, 59), true),
    TEST("Test Ticket", 999.0F, 0, LocalDateTime.MAX, true),
    FREE("Free", 0F, 0, LocalDateTime.MAX, false);

    private final float price;
    private final int limit;
    private final String text;
    private final LocalDateTime deadline;
    private final boolean buyable;

    TicketType(String text, float price, int limit, LocalDateTime deadline, boolean buyable) {
        this.text = text;
        this.price = price;
        this.limit = limit;
        this.deadline = deadline;
        this.buyable = buyable;
    }

    public float getPrice() {
        return price;
    }

    public int getLimit() {
        return limit;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public boolean isBuyable() {
        return buyable;
    }
}
