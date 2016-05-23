package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.exception.ConsumptionNotFoundException;
import ch.wisv.areafiftylan.model.ConsumptionMap;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.util.Consumption;
import ch.wisv.areafiftylan.service.repository.ConsumptionMapsRepository;
import ch.wisv.areafiftylan.service.repository.PossibleConsumptionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Created by beer on 16-5-16.
 */
@Service
public class ConsumptionServiceImpl implements ConsumptionService {
    ConsumptionMapsRepository consumptionMapsRepository;
    PossibleConsumptionsRepository possibleConsumptionsRepository;
    TicketService ticketService;

    @Autowired
    public ConsumptionServiceImpl(ConsumptionMapsRepository consumptionMapsRepository,
                                  PossibleConsumptionsRepository possibleConsumptionsRepository,
                                  TicketService ticketService) {
        this.consumptionMapsRepository = consumptionMapsRepository;
        this.possibleConsumptionsRepository = possibleConsumptionsRepository;
        this.ticketService = ticketService;
    }

    @Override
    public ConsumptionMap getByTicketId(Long ticketId) {
        return consumptionMapsRepository.findByTicketId(ticketId).orElse(InitializeConsumptionMap(ticketId));
    }

    @Override
    public boolean isConsumed(Long ticketId, Long consumptionId) {
        Consumption c = getByConsumptionId(consumptionId);
        return getByTicketId(ticketId).isConsumed(c);
    }

    private ConsumptionMap InitializeConsumptionMap(Long ticketId){
        Ticket t = ticketService.getTicketById(ticketId);
        return consumptionMapsRepository.saveAndFlush(new ConsumptionMap(t));
    }

    @Override
    public void consume(Long ticketId, Long consumptionId) {
        ConsumptionMap consumptions = getByTicketId(ticketId);
        Consumption consumption = getByConsumptionId(consumptionId);
        consumptions.consume(consumption);
        consumptionMapsRepository.saveAndFlush(consumptions);
    }

    @Override
    public void reset(Long ticketId, Long consumptionId) {
        ConsumptionMap consumptions = getByTicketId(ticketId);
        Consumption consumption = getByConsumptionId(consumptionId);
        consumptions.reset(consumption);
        consumptionMapsRepository.saveAndFlush(consumptions);
    }

    @Override
    public Consumption getByConsumptionId(Long consumptionId) {
        return possibleConsumptionsRepository.findById(consumptionId)
                .orElseThrow(() -> new ConsumptionNotFoundException(consumptionId));
    }

    @Override
    public Collection<Consumption> getPossibleConsumptions() {
        return possibleConsumptionsRepository.findAll();
    }

    @Override
    public Consumption removePossibleConsumption(Long consumptionId) {
        Consumption consumption = getByConsumptionId(consumptionId);
        possibleConsumptionsRepository.delete(consumption);
        return consumption;
    }

    @Override
    public Consumption addPossibleConsumption(String consumptionName) {
        if(possibleConsumptionsRepository.findByName(consumptionName).isPresent()){
            throw new DuplicateKeyException("Consumption " + consumptionName + " is already supported");
        }

        Consumption consumption = new Consumption(consumptionName);
        return possibleConsumptionsRepository.saveAndFlush(consumption);
    }
}
