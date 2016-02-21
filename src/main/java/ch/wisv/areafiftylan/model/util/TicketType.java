package ch.wisv.areafiftylan.model.util;

public enum TicketType {
    EARLY_FULL("Early Bird", 37.50F, 50), REGULAR_FULL("Regular", 40.00F, 150);

    private final float price;
    private final int limit;
    private final String text;

    TicketType(String text, float price, int limit) {
        this.text = text;
        this.price = price;
        this.limit = limit;
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
}
