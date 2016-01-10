package ch.wisv.areafiftylan.model;

import ch.wisv.areafiftylan.model.view.View;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;

@Entity
public class Seat {

    @JsonView(View.Public.class)
    public boolean taken;

    @OneToOne(cascade = CascadeType.MERGE)
    @JsonView(View.Public.class)
    public User user;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    public String seatGroup;

    public int seatNumber;

    public Seat(String seatGroup, int seatNumber) {
        this.seatGroup = seatGroup;
        this.seatNumber = seatNumber;
        this.taken = false;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;

        this.taken = this.user != null;
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
