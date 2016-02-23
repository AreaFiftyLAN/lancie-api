package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.exception.NotGoalUserException;
import ch.wisv.areafiftylan.exception.TicketNotTransferrableException;
import ch.wisv.areafiftylan.exception.TicketUnavailableException;
import ch.wisv.areafiftylan.exception.TokenNotFoundException;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import ch.wisv.areafiftylan.util.ResponseEntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public Optional<Ticket> getTicketByKey(String key){
        return ticketRepository.findByKey(key);
    }

    @Override
    public Ticket requestTicketOfType(TicketType type, User owner, boolean pickupService, boolean chMember) {
        if (ticketRepository.countByType(type) >= type.getLimit()) {
            throw new TicketUnavailableException(type);
        } else {
            Ticket ticket = new Ticket(owner, type, pickupService, chMember);
            return ticketRepository.save(ticket);
        }
    }

    @Override
    public void setupForTransfer(Ticket ticket, User goalUser){
        setUpForTransfer(ticket, goalUser);

        ticketRepository.save(ticket);
    }

    @Override
    public void transferTicket(User user, Ticket ticket) {
        if (!ticket.isLockedForTransfer()){
            throw new TicketNotTransferrableException(ticket.getKey());
        }

        if (!ticket.getTransferGoalOwner().equals(user)){
            throw new NotGoalUserException();
        }

        if (ticket.isLockedForTransfer()) {
            finalizeTransfer(ticket);

            ticketRepository.save(ticket);
        } else {
            //TODO: Deal with invalid transfer attempt
        }
    }

    public void setUpForTransfer(Ticket t, User u){
        t.setLockedForTransfer(true);
        t.setTransferGoalOwner(u);
    }

    public void finalizeTransfer(Ticket t){
        if(!t.isLockedForTransfer()) throw new TicketNotTransferrableException(t.getKey());

        t.setLockedForTransfer(false);

        User newOwner = t.getTransferGoalOwner();
        t.setOwner(newOwner);
        t.setTransferGoalOwner(null);
        t.setNewKey();
    }
}
