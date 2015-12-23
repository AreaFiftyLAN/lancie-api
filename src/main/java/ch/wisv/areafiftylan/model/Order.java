package ch.wisv.areafiftylan.model;

import ch.wisv.areafiftylan.model.util.OrderStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;

@Entity
public class Order {

    @Id
    @GeneratedValue
    Long id;

    @OneToMany(cascade = CascadeType.MERGE, targetEntity = Ticket.class)
    Collection<Ticket> tickets;

    OrderStatus status;

    LocalDateTime creationDateTime;

    /**
     * This String can be used to store an external reference. Payment providers often have their own id.
     */
    String reference;

    @ManyToOne
    User user;

    public Order(User user) {
        this.user = user;
        status = OrderStatus.CREATING;
        creationDateTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Collection<Ticket> getTickets() {
        return tickets;
    }

    public boolean addTicket(Ticket ticket){
        return tickets.add(ticket);
    }

    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }

    public String getReference() {
        return reference;
    }

    public User getUser() {
        return user;
    }
}
