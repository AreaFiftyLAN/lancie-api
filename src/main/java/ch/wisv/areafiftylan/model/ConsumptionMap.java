package ch.wisv.areafiftylan.model;

import ch.wisv.areafiftylan.exception.ConsumptionNotSupportedException;

import java.util.HashMap;

/**
 * Created by beer on 8-5-16.
 */
public class ConsumptionMap{
    public static HashMap<String, Boolean> MasterMap;

    private HashMap<String, Boolean> consumptionMap;

    public ConsumptionMap() {
        this.consumptionMap = MasterMap;
    }

    public void consume(String consumption){
        setConsumption(consumption, true);
    }

    public void reset(String consumption){
        setConsumption(consumption, false);
    }

    private void setConsumption(String consumption, boolean value){
        if(!MasterMap.containsKey(consumption)){
            throw new ConsumptionNotSupportedException(consumption);
        }

        consumptionMap.put(consumption, value);
    }
}
