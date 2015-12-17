package ch.wisv.areafiftylan.model.util;

import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.view.View;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sille on 17-11-15.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "groupname", columnNames = { "name" }))
public class SeatGroup {

    @Id
    @JsonView(View.Public.class)
    String name;

    @JsonView(View.Public.class)
    int capacity;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonView(View.Public.class)
    Map<Integer, Seat> seats;

    public SeatGroup(String name, int capacity) {
        this.seats = new HashMap<>();

        for (int i = 0; i < capacity; i++) {
            seats.put(i, new Seat());
        }
        this.capacity = capacity;
        this.name = name;
    }

    public SeatGroup() {
        //JPA ONLY
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Map<Integer, Seat> getSeats() {
        return seats;
    }

    public void setSeats(Map<Integer, Seat> seats) {
        this.seats = seats;
    }
}
