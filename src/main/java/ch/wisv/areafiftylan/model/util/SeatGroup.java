package ch.wisv.areafiftylan.model.util;

import ch.wisv.areafiftylan.model.Seat;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sille on 17-11-15.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "groupname", columnNames = { "name" }))
public class SeatGroup {

    String name;

    int capacity;

    @ElementCollection(targetClass = Seat.class)
    Set<Seat> seats;

    public SeatGroup(String name, int capacity) {
        this.seats = new HashSet<>(capacity);
        this.capacity = capacity;
        this.name = name;
    }
}
