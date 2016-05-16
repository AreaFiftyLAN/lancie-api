package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.ConsumptionMap;
import ch.wisv.areafiftylan.service.repository.ConsumptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by beer on 16-5-16.
 */
@Service
public interface ConsumptionService {
    ConsumptionMap getByTicketId(Long ticketId);

    void consume(Long ticketId, String consumption);

    void reset(Long ticketId, String consumption);

    //TODO: Make logic for setting the master consumption list
}
