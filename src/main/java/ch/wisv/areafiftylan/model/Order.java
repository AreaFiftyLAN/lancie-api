package ch.wisv.areafiftylan.model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Collection;
import java.util.HashSet;

public class Order {

    @Id
    @GeneratedValue
    Long id;

    double amount;

    Collection<Ticket> tickets = new HashSet<>();

    boolean paid;

    @ManyToOne
    User user;

    public Order(double amount, User user, Collection<Ticket> tickets) {
        this.amount = amount;
        this.user = user;
        this.tickets = tickets;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
