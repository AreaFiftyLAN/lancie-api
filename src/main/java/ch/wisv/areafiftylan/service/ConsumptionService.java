package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.ConsumptionMap;
import ch.wisv.areafiftylan.model.util.Consumption;
import ch.wisv.areafiftylan.service.repository.ConsumptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Created by beer on 16-5-16.
 */
public interface ConsumptionService {
    ConsumptionMap getByTicketId(Long ticketId);

    void consume(Long ticketId, Consumption consumption);

    void reset(Long ticketId, Consumption consumption);

    Collection<Consumption> getPossibleConsumptions();

    void removePossibleConsumption(Consumption consumption);

    void addPossibleConsumption(Consumption consumption);

    //TODO: Make logic for setting the master consumption list
}
