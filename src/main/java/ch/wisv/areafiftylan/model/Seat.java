package ch.wisv.areafiftylan.model;

import ch.wisv.areafiftylan.model.util.Coordinate;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class Seat {

    boolean taken;
    @OneToOne(cascade = CascadeType.MERGE)
    User user;
    @Id
    private Coordinate coordinate;

    public Seat(Coordinate coordinate) {
        this.coordinate = coordinate;
        this.taken = false;
    }

    public Seat() {
        //JPA only
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

    public Coordinate getCoordinate() {
        return coordinate;
    }
}
