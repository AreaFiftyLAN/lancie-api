package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.exception.DuplicateTicketTransferTokenException;
import ch.wisv.areafiftylan.exception.InvalidTokenException;
import ch.wisv.areafiftylan.exception.TicketUnavailableException;
import ch.wisv.areafiftylan.exception.TokenNotFoundException;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.security.token.TicketTransferToken;
import ch.wisv.areafiftylan.security.token.Token;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import ch.wisv.areafiftylan.service.repository.token.TicketTransferTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {
    private TicketRepository ticketRepository;
    private UserService userService;
    private TicketTransferTokenRepository tttRepository;
    private MailService mailService;
    private TeamService teamService;

    @Value("${a5l.user.acceptTransferUrl}")
    private String acceptTransferUrl;

    @Autowired
    public TicketServiceImpl(TicketRepository ticketRepository, UserService userService, TicketTransferTokenRepository tttRepository,
                             MailService mailService, TeamService teamService) {
        this.ticketRepository = ticketRepository;
        this.userService = userService;
        this.tttRepository = tttRepository;
        this.mailService = mailService;
        this.teamService = teamService;
    }

    @Override
    public Ticket getTicketById(Long ticketId){
        return ticketRepository.findOne(ticketId);
    }

    @Override
    public Ticket removeTicket(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        ticketRepository.delete(ticket);
        return ticket;
    }

    @Override
    public Integer getNumberSoldOfType(TicketType type) {
        return ticketRepository.countByType(type);
    }

    @Override
    public Collection<Ticket> findValidTicketsByOwnerUsername(String username) {
        return ticketRepository.findAllByOwnerUsername(username).stream()
                .filter(Ticket::isValid)
                .collect(Collectors.toList());
    }

    @Override
    public void validateTicket(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        ticket.setValid(true);
        ticketRepository.save(ticket);
    }

    @Override
    public synchronized Ticket requestTicketOfType(TicketType type, User owner, boolean pickupService, boolean chMember) {
        if (ticketRepository.countByType(type) >= type.getLimit()) {
            throw new TicketUnavailableException(type);
        } else {
            Ticket ticket = new Ticket(owner, type, pickupService, chMember);
            return ticketRepository.save(ticket);
        }
    }

    @Override
    public TicketTransferToken setupForTransfer(Long ticketId, String goalUserName){
        User u = userService.getUserByUsername(goalUserName).orElseThrow(() -> new UsernameNotFoundException("User " + goalUserName + " not found."));
        Ticket t = ticketRepository.findOne(ticketId);

        List<TicketTransferToken> ticketTransferTokens = tttRepository.findAllByTicketId(ticketId).stream()
                .filter(Token::isValid)
                .collect(Collectors.toList());

        if (!ticketTransferTokens.isEmpty()) {
            throw new DuplicateTicketTransferTokenException(ticketId);
        } else {
            TicketTransferToken ttt = new TicketTransferToken(u, t);

            tttRepository.save(ttt);

            try {
                String acceptUrl = acceptTransferUrl + "?token=" + ttt.getToken();
                mailService.sendTicketTransferMail(ttt.getTicket().getOwner(), ttt.getUser(), acceptUrl);
            } catch (MessagingException e) {
                e.printStackTrace();
            }

            return ttt;
        }
    }

    @Override
    public void transferTicket(String token) {
        TicketTransferToken ttt = getTicketTransferTokenIfValid(token);
        Ticket t = ttt.getTicket();

        User newOwner = ttt.getUser();
        t.setOwner(newOwner);

        ticketRepository.save(t);

        ttt.use();

        tttRepository.save(ttt);
    }

    @Override
    public void cancelTicketTransfer(String token){
        TicketTransferToken ttt = getTicketTransferTokenIfValid(token);

        ttt.revoke();

        tttRepository.save(ttt);
    }

    @Override
    public Collection<TicketTransferToken> getValidTicketTransferTokensByUser(String username) {
        return tttRepository.findAllByTicketOwnerUsername(username).stream()
                .filter(TicketTransferToken::isValid)
                .collect(Collectors.toList());
    }

    private TicketTransferToken getTicketTransferTokenIfValid(String token){
        TicketTransferToken ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));

        //Check validity of the token
        if (!ttt.isValid()) {
            throw new InvalidTokenException();
        }

        return ttt;
    }

    @Override
    public Collection<Ticket> getTicketsInControl(User u){
        Collection<Ticket> ownedTickets = ticketRepository.findAllByOwnerUsername(u.getUsername());
        Collection<Ticket> captainedTickets = getCaptainedTickets(u);

        Collection<Ticket> ticketsInControl = new ArrayList<>();
        ticketsInControl.addAll(ownedTickets);
        ticketsInControl.addAll(captainedTickets);

        return ticketsInControl;
    }

    private Collection<Ticket> getCaptainedTickets(User u){
        Collection<Team> captainedTeams = teamService.getTeamByCaptainId(u.getId());

        return captainedTeams.stream()
                .flatMap(t -> t.getMembers().stream())
                .filter(m -> !m.equals(u))
                .flatMap(m -> ticketRepository.findAllByOwnerUsername(m.getUsername()).stream())
                .collect(Collectors.toList());
    }
}
