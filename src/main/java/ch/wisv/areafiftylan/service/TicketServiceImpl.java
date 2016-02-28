package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.exception.TicketNotFoundException;
import ch.wisv.areafiftylan.exception.TicketNotTransferrableException;
import ch.wisv.areafiftylan.exception.TicketUnavailableException;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TicketServiceImpl implements TicketService {

    private TicketRepository ticketRepository;
    private UserService userService;

    @Autowired
    public TicketServiceImpl(TicketRepository ticketRepository, UserService userService) {
        this.ticketRepository = ticketRepository;
        this.userService = userService;
    }

    @Override
    public Ticket getTicketByKey(String key){
        return ticketRepository.findByKey(key).orElseThrow(() -> new TicketNotFoundException("Ticket with key " + key + " not found."));
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
    public void setupForTransfer(String ticketKey, String goalUserName){
        User u = userService.getUserByUsername(goalUserName).orElseThrow(() -> new UsernameNotFoundException("User " + goalUserName + " not found."));
        Ticket t = ticketRepository.findByKey(ticketKey).orElseThrow(() -> new TicketNotFoundException(ticketKey));

        t.setTransferrable(true);
        t.setTransferGoalOwner(u);

        ticketRepository.save(t);
    }

    @Override
    public void transferTicket(String ticketKey) {
        Ticket ticket = ticketRepository.findByKey(ticketKey).orElseThrow(() -> new TicketNotFoundException(ticketKey));

        if (ticket.isTransferrable()) {
            finalizeTransfer(ticket);

            ticketRepository.save(ticket);
        } else {
            throw new TicketNotTransferrableException(ticket.getKey());
        }
    }

    public void finalizeTransfer(Ticket t){
        if(!t.isTransferrable()) throw new TicketNotTransferrableException(t.getKey());

        t.setTransferrable(false);

        User newOwner = t.getTransferGoalOwner();
        t.setOwner(newOwner);
        t.setTransferGoalOwner(null);
        t.setNewKey();
    }
}
