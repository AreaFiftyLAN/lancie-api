package ch.wisv.areafiftylan.model;

import ch.wisv.areafiftylan.model.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "seatConstraint", columnNames = { "seatGroup","seatNumber" }) } )
public class Seat {

    @JsonView(View.Public.class)
    public boolean taken;

    @OneToOne(cascade = CascadeType.MERGE)
    @JsonView(View.Public.class)
    public Ticket ticket;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    @JsonView(View.Public.class)
    public String seatGroup;

    @JsonView(View.Public.class)
    public int seatNumber;

    public Seat(String seatGroup, int seatNumber) {
        this.seatGroup = seatGroup;
        this.seatNumber = seatNumber;
        this.taken = false;
    }

    public Seat(){
        //JPA ONLY
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    @JsonIgnore
    public User getUser() {
        return ticket.getOwner();
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;

        this.taken = this.ticket != null;
    }

    public String getSeatGroup() {
        return seatGroup;
    }

    public void setSeatGroup(String seatGroup) {
        this.seatGroup = seatGroup;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }
}
