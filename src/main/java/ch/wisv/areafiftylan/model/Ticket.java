package ch.wisv.areafiftylan.model;

import ch.wisv.areafiftylan.model.util.TicketType;

import javax.persistence.*;

@Entity
public class Ticket {

    @Id
    @GeneratedValue
    Long id;

    @OneToOne(mappedBy = "ticket")
    User user;

    @Enumerated(EnumType.STRING)
    TicketType type;

    Boolean pickupService;

    public Ticket(User user, TicketType type, Boolean pickupService) {
        this.user = user;
        this.type = type;
        this.pickupService = pickupService;
    }
}
