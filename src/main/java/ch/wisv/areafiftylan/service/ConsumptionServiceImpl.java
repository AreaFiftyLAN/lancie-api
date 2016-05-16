package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.exception.ConsumptionNotSupportedException;
import ch.wisv.areafiftylan.model.ConsumptionMap;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.service.repository.ConsumptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by beer on 16-5-16.
 */
public class ConsumptionServiceImpl implements ConsumptionService {
    @Autowired
    ConsumptionRepository consumptionRepository;

    @Autowired
    TicketService ticketService;

    @Override
    public ConsumptionMap getByTicketId(Long ticketId) {
        return consumptionRepository.findByTicketId(ticketId).orElse(InitializeConsumptionMap(ticketId));
    }

    private ConsumptionMap InitializeConsumptionMap(Long ticketId){
        Ticket t = ticketService.getTicketById(ticketId);
        return consumptionRepository.saveAndFlush(new ConsumptionMap(t));
    }

    @Override
    public void consume(Long ticketId, String consumption) {
        ConsumptionMap consumptions = getByTicketId(ticketId);
        consumptions.consume(consumption);
        consumptionRepository.saveAndFlush(consumptions);
    }

    @Override
    public void reset(Long ticketId, String consumption) {
        ConsumptionMap consumptions = getByTicketId(ticketId);
        consumptions.reset(consumption);
        consumptionRepository.saveAndFlush(consumptions);
    }

    @Override
    public Collection<String> getPossibleConsumptions() {
        return ConsumptionMap.PossibleConsumptions;
    }

    @Override
    public void removePossibleConsumption(String consumption) {
        if(!ConsumptionMap.PossibleConsumptions.contains(consumption)){
            throw new ConsumptionNotSupportedException(consumption);
        }

        ConsumptionMap.PossibleConsumptions.remove(consumption);
    }

    @Override
    public void addPossibleConsumption(String consumption) {
        if(ConsumptionMap.PossibleConsumptions.contains(consumption)){
            throw new DuplicateKeyException("Consumption " + consumption + " is already supported");
        }

        ConsumptionMap.PossibleConsumptions.add(consumption);
    }
}
