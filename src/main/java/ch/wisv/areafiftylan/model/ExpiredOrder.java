package ch.wisv.areafiftylan.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The ExpiredOrder class is made to keep track of expired Orders. When an order is expired, all the relevant data is
 * stored in String format for reference.
 */
@Entity
public class ExpiredOrder {
    @Id
    Long id;

    Collection<Map<String, String>> tickets;

    String createdAt;

    String expiredAt;

    String createdBy;

    public ExpiredOrder(Long id, Collection<Ticket> tickets, String createdAt, String expiredAt, String createdBy) {
        this.id = id;
        this.tickets = new ArrayList<>();
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
        this.createdBy = createdBy;

        tickets.forEach(t -> this.tickets.add(ticketToMap(t)));
    }

    public ExpiredOrder(Order order) {
        this.id = order.getId();
        this.tickets = new ArrayList<>();
        this.createdAt = order.getCreationDateTime().toString();
        this.expiredAt = LocalDateTime.now().toString();

        order.getTickets().forEach(t -> tickets.add(ticketToMap(t)));
    }

    private Map<String, String> ticketToMap(Ticket ticket) {
        Map<String, String> map = new HashMap<>();

        map.put("type", ticket.getType().toString());
        map.put("pickupService", String.valueOf(ticket.hasPickupService()));
        map.put("price", String.valueOf(ticket.getPrice()));

        return map;
    }
}
