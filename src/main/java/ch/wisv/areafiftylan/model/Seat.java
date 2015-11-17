package ch.wisv.areafiftylan.model;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "groupname", columnNames = { "seatgroup" }))
public class Seat {

    boolean taken;

    @OneToOne(cascade = CascadeType.MERGE)
    User user;

    String seatgroup;

    int seatnumber;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    public Seat(String seatgroup, int seatnumber) {
        this.seatgroup = seatgroup;
        this.seatnumber = seatnumber;
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

    public String getSeatgroup() {
        return seatgroup;
    }

    public void setSeatgroup(String seatgroup) {
        this.seatgroup = seatgroup;
    }

    public int getSeatnumber() {
        return seatnumber;
    }

    public void setSeatnumber(int seatnumber) {
        this.seatnumber = seatnumber;
    }
}
