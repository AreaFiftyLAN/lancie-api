package ch.wisv.areafiftylan.security.token;

import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Created by beer on 11-3-16.
 */
@Entity
public class TicketTransferToken extends Token {
    private static final int EXPIRATION = 0;

    @OneToOne(targetEntity = Ticket.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private Ticket ticket;

    public TicketTransferToken(){
    }

    public TicketTransferToken(User user, Ticket ticket) {
        super(user, EXPIRATION);
        this.ticket = ticket;
    }

    public Ticket getTicket(){
        return ticket;
    }
}
