package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.ConsumptionMap;
import ch.wisv.areafiftylan.service.repository.ConsumptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Created by beer on 16-5-16.
 */
public interface ConsumptionService {
    ConsumptionMap getByTicketId(Long ticketId);

    void consume(Long ticketId, String consumption);

    void reset(Long ticketId, String consumption);

    Collection<String> getPossibleConsumptions();

    void removePossibleConsumption(String consumption);

    void addPossibleConsumption(String consumption);

    //TODO: Make logic for setting the master consumption list
}
