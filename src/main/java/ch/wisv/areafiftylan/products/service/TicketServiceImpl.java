/*
 * Copyright (c) 2016  W.I.S.V. 'Christiaan Huygens'
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.wisv.areafiftylan.products.service;

import ch.wisv.areafiftylan.exception.*;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.utils.mail.MailService;
import ch.wisv.areafiftylan.extras.rfid.service.RFIDService;
import ch.wisv.areafiftylan.teams.service.TeamService;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.products.model.TicketType;
import ch.wisv.areafiftylan.security.token.TicketTransferToken;
import ch.wisv.areafiftylan.security.token.Token;
import ch.wisv.areafiftylan.security.token.repository.TicketTransferTokenRepository;
import ch.wisv.areafiftylan.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {
    private TicketRepository ticketRepository;
    private UserService userService;
    private TicketTransferTokenRepository tttRepository;
    private MailService mailService;
    private TeamService teamService;
    private RFIDService rfidService;

    @Value("${a5l.user.acceptTransferUrl}")
    private String acceptTransferUrl;

    @Value("${a5l.ticketLimit}")
    private int TICKET_LIMIT;

    @Autowired
    public TicketServiceImpl(TicketRepository ticketRepository, UserService userService,
                             TicketTransferTokenRepository tttRepository, MailService mailService,
                             TeamService teamService) {
        this.ticketRepository = ticketRepository;
        this.userService = userService;
        this.tttRepository = tttRepository;
        this.mailService = mailService;
        this.teamService = teamService;
    }

    @Autowired
    public void setRFIDService(RFIDService rfidService){
        this.rfidService = rfidService;
    }

    @Override
    public Ticket getTicketById(Long ticketId) {
        Ticket t = ticketRepository.findOne(ticketId);

        if (t == null) {
            throw new TicketNotFoundException();
        }

        return t;
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
        return ticketRepository.findAllByOwnerUsernameIgnoreCase(username).stream().filter(Ticket::isValid)
                .collect(Collectors.toList());
    }

    @Override
    public void validateTicket(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        ticket.setValid(true);
        ticketRepository.save(ticket);
    }

    @Override
    public synchronized Ticket requestTicketOfType(TicketType type, User owner, boolean pickupService,
                                                   boolean chMember) {
        // Check if the TicketType has a limit and if the limit is reached
        if (!isTicketAvailable(type)) {
            throw new TicketUnavailableException(type);
        } else {
            Ticket ticket = new Ticket(owner, type, pickupService, chMember);
            return ticketRepository.save(ticket);
        }
    }

    private boolean isTicketAvailable(TicketType type) {
        boolean typeLimitReached = type.getLimit() != 0 && ticketRepository.countByType(type) >= type.getLimit();
        boolean eventLimitReached = ticketRepository.count() >= TICKET_LIMIT;
        boolean deadlineExceeded = type.getDeadline().isBefore(LocalDateTime.now());

        return !typeLimitReached && !deadlineExceeded && !eventLimitReached;
    }


    @Override
    public TicketTransferToken setupForTransfer(Long ticketId, String goalUserName) {
        User u = userService.getUserByUsername(goalUserName)
                .orElseThrow(() -> new UsernameNotFoundException("User " + goalUserName + " not found."));
        Ticket t = getTicketById(ticketId);

        List<TicketTransferToken> ticketTransferTokens =
                tttRepository.findAllByTicketId(ticketId).stream().filter(Token::isValid).collect(Collectors.toList());

        if (!ticketTransferTokens.isEmpty()) {
            throw new DuplicateTicketTransferTokenException(ticketId);
        } if(rfidService.isTicketLinked(ticketId)) {
            throw new TicketAlreadyLinkedException();
        } else {
            TicketTransferToken ttt = new TicketTransferToken(u, t);

            ttt = tttRepository.save(ttt);

            String acceptUrl = acceptTransferUrl + "?token=" + ttt.getToken();
            mailService.sendTicketTransferMail(ttt.getTicket().getOwner(), ttt.getUser(), acceptUrl);

            return ttt;
        }
    }

    @Override
    public void transferTicket(String token) {
        TicketTransferToken ttt = getTicketTransferTokenIfValid(token);
        Ticket t = ttt.getTicket();

        if(rfidService.isTicketLinked(t.getId())){
            throw new TicketAlreadyLinkedException();
        }

        User newOwner = ttt.getUser();
        t.setOwner(newOwner);

        ticketRepository.save(t);

        ttt.use();

        tttRepository.save(ttt);
    }

    @Override
    public void cancelTicketTransfer(String token) {
        TicketTransferToken ttt = getTicketTransferTokenIfValid(token);

        ttt.revoke();

        tttRepository.save(ttt);
    }

    @Override
    public Collection<TicketTransferToken> getValidTicketTransferTokensByUser(String username) {
        return tttRepository.findAllByTicketOwnerUsernameIgnoreCase(username).stream()
                .filter(TicketTransferToken::isValid).collect(Collectors.toList());
    }

    @Override
    public Collection<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Override
    public Collection<Ticket> getAllTicketsWithTransport() {
        return ticketRepository.findByPickupService_True();
    }

    private TicketTransferToken getTicketTransferTokenIfValid(String token) {
        TicketTransferToken ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));

        //Check validity of the token
        if (!ttt.isValid()) {
            throw new InvalidTokenException();
        }

        return ttt;
    }

    @Override
    public Collection<Ticket> getOwnedTicketsAndFromTeamMembers(User u) {
        Collection<Ticket> ownedTickets =
                ticketRepository.findAllByOwnerUsernameIgnoreCase(u.getUsername()).stream().filter(Ticket::isValid)
                        .collect(Collectors.toList());
        Collection<Ticket> captainedTickets = getCaptainedTickets(u);

        Collection<Ticket> ticketsInControl = new ArrayList<>();
        ticketsInControl.addAll(ownedTickets);
        ticketsInControl.addAll(captainedTickets);

        return ticketsInControl;
    }

    private Collection<Ticket> getCaptainedTickets(User u) {
        Collection<Team> captainedTeams = teamService.getTeamByCaptainId(u.getId());

        return captainedTeams.stream()
                .flatMap(t -> t.getMembers().stream()).filter(m -> !m.equals(u))
                .flatMap(m -> ticketRepository.findAllByOwnerUsernameIgnoreCase(m.getUsername()).stream()
                        .filter(Ticket::isValid))
                .collect(Collectors.toList());
    }
}
