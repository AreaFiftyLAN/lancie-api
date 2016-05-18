package ch.wisv.areafiftylan.model.util;

import java.time.LocalDateTime;

public enum TicketType {
    EARLY_FULL("Early Bird", 37.50F, 50, LocalDateTime.of(2016, 6, 3, 0, 0)),
    REGULAR_FULL("Regular", 40.00F, 0, LocalDateTime.of(2016, 5, 29, 0, 0)),
    LAST_MINUTE("Last Minute", 45.00F, 0, LocalDateTime.of(2016, 5, 29, 0, 0));

    private final float price;
    private final int limit;
    private final String text;
    private final LocalDateTime deadline;

    TicketType(String text, float price, int limit, LocalDateTime deadline) {
        this.text = text;
        this.price = price;
        this.limit = limit;
        this.deadline = deadline;
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
}
