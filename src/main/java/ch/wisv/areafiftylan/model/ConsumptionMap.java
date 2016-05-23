package ch.wisv.areafiftylan.model;

import ch.wisv.areafiftylan.exception.AlreadyConsumedException;
import ch.wisv.areafiftylan.model.util.Consumption;
import lombok.Getter;
import lombok.NonNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by beer on 8-5-16.
 */
@Entity
public class ConsumptionMap {
    @Id
    @GeneratedValue
    Long id;

    @NonNull
    @ElementCollection
    private Collection<Consumption> consumptionsMade;

    @OneToOne(targetEntity = Ticket.class, cascade = CascadeType.MERGE)
    @NonNull
    @Getter
    private Ticket ticket;

    public ConsumptionMap() {
        // JPA Only
    }

    public ConsumptionMap(Ticket t) {
        this.consumptionsMade = new ArrayList<>();
        this.ticket = t;
    }

    public boolean isConsumed(Consumption consumption){
        return consumptionsMade.contains(consumption);
    }

    public void consume(Consumption consumption){
        if(isConsumed(consumption)){
            throw new AlreadyConsumedException(consumption);
        }

        consumptionsMade.add(consumption);
    }

    public void reset(Consumption consumption){
        if(isConsumed(consumption)){
            consumptionsMade.remove(consumption);
        }
    }

    public Collection<Consumption> getConsumptionsMade(){
        return consumptionsMade;
    }
}
