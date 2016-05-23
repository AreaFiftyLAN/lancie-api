package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.ConsumptionMap;
import ch.wisv.areafiftylan.model.util.Consumption;

import java.util.Collection;

/**
 * Created by beer on 16-5-16.
 */
public interface ConsumptionService {
    ConsumptionMap getByTicketId(Long ticketId);

    boolean isConsumed(Long ticketId, Long consumptionId);

    void consume(Long ticketId, Long consumptionId);

    void reset(Long ticketId, Long consumptionId);

    Consumption getByConsumptionId(Long consumptionId);

    Collection<Consumption> getPossibleConsumptions();

    Consumption removePossibleConsumption(Long consumptionId);

    Consumption addPossibleConsumption(String consumptionName);
}
