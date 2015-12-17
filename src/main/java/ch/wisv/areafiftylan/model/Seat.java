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

    public Seat() {
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


}
