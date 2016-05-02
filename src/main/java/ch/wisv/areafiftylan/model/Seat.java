package ch.wisv.areafiftylan.model;

import ch.wisv.areafiftylan.model.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "seatConstraint", columnNames = { "seatGroup", "seatNumber" }) })
public class Seat {

    @Getter
    @Setter
    @JsonView(View.Public.class)
    public boolean taken;

    @Getter
    @OneToOne(cascade = CascadeType.MERGE)
    @JsonView(View.Public.class)
    public Ticket ticket;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    @Getter
    @Setter
    @JsonView(View.Public.class)
    public String seatGroup;

    @Getter
    @Setter
    @JsonView(View.Public.class)
    public int seatNumber;

    public Seat(String seatGroup, int seatNumber) {
        this.seatGroup = seatGroup;
        this.seatNumber = seatNumber;
        this.taken = false;
    }

    public Seat() {
        //JPA ONLY
    }

    @JsonIgnore
    public User getUser() {
        return ticket.getOwner();
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;

        this.taken = this.ticket != null;
    }
}
