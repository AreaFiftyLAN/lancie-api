package ch.wisv.areafiftylan.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

/**
 * The ExpiredOrder class is made to keep track of expired Orders. When an order is expired, all the relevant data is
 * stored in String format for reference.
 */
@Entity
public class ExpiredOrder {
    @Id
    Long id;

    int numberOfTickets;

    String createdAt;

    String expiredAt;

    String createdBy;

    public ExpiredOrder(){
        //JPA Only
    }

    public ExpiredOrder(Long id, int numberOfTickets, String createdAt, String expiredAt, String createdBy) {
        this.id = id;
        this.numberOfTickets = numberOfTickets;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
        this.createdBy = createdBy;

    }

    public ExpiredOrder(Order order) {
        this.id = order.getId();
        this.numberOfTickets = order.getTickets().size();
        this.createdAt = order.getCreationDateTime().toString();
        this.expiredAt = LocalDateTime.now().toString();
        this.createdBy = order.getUser().getUsername();

    }
}
