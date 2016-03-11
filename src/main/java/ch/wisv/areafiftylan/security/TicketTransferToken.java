package ch.wisv.areafiftylan.security;

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

    @OneToOne(targetEntity = Ticket.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private Ticket ticket;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private User goalUser;

    public TicketTransferToken(){
    }

    public TicketTransferToken(String token, User user, Ticket ticket, User goalUser) {
        super(token, user);
        this.ticket = ticket;
        this.goalUser = goalUser;
    }

    public Ticket getTicket(){
        return ticket;
    }

    public User getGoalUser() { return goalUser; }
}
