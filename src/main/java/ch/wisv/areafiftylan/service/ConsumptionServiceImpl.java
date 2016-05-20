package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.exception.ConsumptionNotSupportedException;
import ch.wisv.areafiftylan.model.ConsumptionMap;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.util.Consumption;
import ch.wisv.areafiftylan.service.repository.ConsumptionRepository;
import ch.wisv.areafiftylan.service.repository.PossibleConsumptionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by beer on 16-5-16.
 */
@Service
public class ConsumptionServiceImpl implements ConsumptionService {
    ConsumptionRepository consumptionRepository;
    PossibleConsumptionsRepository possibleConsumptionsRepository;
    TicketService ticketService;

    @Autowired
    public ConsumptionServiceImpl(ConsumptionRepository consumptionRepository,
                                  PossibleConsumptionsRepository possibleConsumptionsRepository,
                                  TicketService ticketService) {
        this.consumptionRepository = consumptionRepository;
        this.possibleConsumptionsRepository = possibleConsumptionsRepository;
        this.ticketService = ticketService;

        ConsumptionMap.PossibleConsumptions = possibleConsumptionsRepository.findAll();
    }

    @Override
    public ConsumptionMap getByTicketId(Long ticketId) {
        return consumptionRepository.findByTicketId(ticketId).orElse(InitializeConsumptionMap(ticketId));
    }

    private ConsumptionMap InitializeConsumptionMap(Long ticketId){
        Ticket t = ticketService.getTicketById(ticketId);
        return consumptionRepository.saveAndFlush(new ConsumptionMap(t));
    }

    @Override
    public void consume(Long ticketId, Consumption consumption) {
        ConsumptionMap consumptions = getByTicketId(ticketId);
        consumptions.consume(consumption);
        consumptionRepository.saveAndFlush(consumptions);
    }

    @Override
    public void reset(Long ticketId, Consumption consumption) {
        ConsumptionMap consumptions = getByTicketId(ticketId);
        consumptions.reset(consumption);
        consumptionRepository.saveAndFlush(consumptions);
    }

    @Override
    public Collection<Consumption> getPossibleConsumptions() {
        return ConsumptionMap.PossibleConsumptions;
    }

    @Override
    public void removePossibleConsumption(Consumption consumption) {
        if(!ConsumptionMap.PossibleConsumptions.contains(consumption)){
            throw new ConsumptionNotSupportedException(consumption);
        }

        ConsumptionMap.PossibleConsumptions.remove(consumption);
        possibleConsumptionsRepository.delete(consumption);
    }

    @Override
    public void addPossibleConsumption(Consumption consumption) {
        if(ConsumptionMap.PossibleConsumptions.contains(consumption)){
            throw new DuplicateKeyException("Consumption " + consumption + " is already supported");
        }

        ConsumptionMap.PossibleConsumptions.add(consumption);
        possibleConsumptionsRepository.saveAndFlush(consumption);
    }
}
