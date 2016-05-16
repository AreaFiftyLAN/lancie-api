package ch.wisv.areafiftylan.model;

import ch.wisv.areafiftylan.exception.AlreadyConsumedException;
import ch.wisv.areafiftylan.exception.ConsumptionNotSupportedException;
import lombok.Getter;
import lombok.NonNull;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by beer on 8-5-16.
 */
@Entity
public class ConsumptionMap {
    public static Collection<String> PossibleConsumptions;

    @NonNull
    private Collection<String> consumptionsMade;

    @OneToOne(targetEntity = Ticket.class, cascade = CascadeType.MERGE)
    @NonNull
    @Getter
    private Ticket ticket;

    public ConsumptionMap() {
        this.consumptionsMade = new ArrayList<>();
    }

    public boolean isConsumed(String consumption){
        checkIfConsumptionAllowedAndThrowIfNot(consumption);

        return consumptionsMade.contains(consumption);
    }

    public void consume(String consumption){
        checkIfConsumptionAllowedAndThrowIfNot(consumption);

        if(isConsumed(consumption)){
            throw new AlreadyConsumedException(consumption);
        }

        consumptionsMade.add(consumption);
    }

    public void reset(String consumption){
        checkIfConsumptionAllowedAndThrowIfNot(consumption);

        if(isConsumed(consumption)){
            consumptionsMade.remove(consumption);
        }
    }

    private void checkIfConsumptionAllowedAndThrowIfNot(String consumption){
        if(!PossibleConsumptions.contains(consumption)) {
            throw new ConsumptionNotSupportedException(consumption);
        }
    }
}
