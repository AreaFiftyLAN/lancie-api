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
import ch.wisv.areafiftylan.extras.rfid.service.RFIDService;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.model.TicketOption;
import ch.wisv.areafiftylan.products.model.TicketType;
import ch.wisv.areafiftylan.products.service.repository.TicketOptionRepository;
import ch.wisv.areafiftylan.products.service.repository.TicketRepository;
import ch.wisv.areafiftylan.products.service.repository.TicketTypeRepository;
import ch.wisv.areafiftylan.security.token.TicketTransferToken;
import ch.wisv.areafiftylan.security.token.Token;
import ch.wisv.areafiftylan.security.token.repository.TicketTransferTokenRepository;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.teams.service.TeamService;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.users.service.UserService;
import ch.wisv.areafiftylan.utils.mail.MailService;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final TicketTransferTokenRepository tttRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketOptionRepository ticketOptionRepository;
    private final MailService mailService;
    private final TeamService teamService;
    private RFIDService rfidService;

    @Value("${a5l.user.acceptTransferUrl}")
    private String acceptTransferUrl;

    @Value("${a5l.ticketLimit}")
    private int TICKET_LIMIT;

    @Autowired
    public TicketServiceImpl(TicketRepository ticketRepository, UserService userService,
                             TicketTransferTokenRepository tttRepository, TicketTypeRepository ticketTypeRepository,
                             TicketOptionRepository ticketOptionRepository, MailService mailService,
                             TeamService teamService) {
        this.ticketRepository = ticketRepository;
        this.tttRepository = tttRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.userService = userService;
        this.ticketOptionRepository = ticketOptionRepository;
        this.mailService = mailService;
        this.teamService = teamService;
    }

    @Autowired
    public void setRFIDService(RFIDService rfidService) {
        this.rfidService = rfidService;
    }

    @Override
    public Ticket getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId).
                orElseThrow(TicketNotFoundException::new);
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
    public Collection<Ticket> findValidTicketsByOwnerEmail(String email) {
        return ticketRepository.findAllByOwnerEmailIgnoreCase(email).stream().
                filter(Ticket::isValid).
                collect(Collectors.toList());
    }

    @Override
    public void validateTicket(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        ticket.setValid(true);
        ticketRepository.save(ticket);
    }

    private TicketOption getTicketOptionByName(String name) {
        return ticketOptionRepository.findByName(name).
                orElseThrow(TicketOptionNotFoundException::new);
    }

    @Override
    public Ticket requestTicketOfType(User user, String type, List<String> options) {
        // Make sure we're not passing null
        options = (options == null) ? Collections.emptyList() : options;

        TicketType ticketType = ticketTypeRepository.findByName(type).
                orElseThrow(() -> new TicketTypeNotFoundException(type));

        List<TicketOption> ticketOptions = options.stream().
                map(this::getTicketOptionByName).
                collect(Collectors.toList());

        return requestTicketOfType(user, ticketType, ticketOptions);
    }

    @Override
    @Synchronized
    public Ticket requestTicketOfType(User user, TicketType type, List<TicketOption> options) {
        if (options == null) {
            options = Collections.emptyList();
        }
        // Check if the TicketType has a numberAvailable and if the numberAvailable is reached
        if (!isTicketAvailable(type)) {
            throw new TicketUnavailableException();
        } else {
            Ticket ticket = new Ticket(user, type);
            // If one of the ticketOptions is not supported
            for (TicketOption option : options) {
                if (!ticket.addOption(option)) {
                    throw new TicketOptionNotSupportedException(option);
                }
            }
            return ticketRepository.save(ticket);
        }
    }

    private boolean isTicketAvailable(TicketType type) {
        if (type == null) {
            return false;
        }
        boolean typeLimitReached =
                type.getNumberAvailable() != 0 && ticketRepository.countByType(type) >= type.getNumberAvailable();
        boolean eventLimitReached = ticketRepository.count() >= TICKET_LIMIT;
        boolean deadlineExceeded = type.getDeadline().isBefore(LocalDateTime.now());

        return !typeLimitReached && !deadlineExceeded && !eventLimitReached;
    }


    @Override
    public TicketTransferToken setupForTransfer(Long ticketId, String receiverEmail) {
        User u = userService.getUserByEmail(receiverEmail);
        Ticket t = getTicketById(ticketId);

        List<TicketTransferToken> ticketTransferTokens = tttRepository.findAllByTicketId(ticketId).stream().
                filter(Token::isValid).
                collect(Collectors.toList());

        if (!ticketTransferTokens.isEmpty()) {
            throw new TicketTransferTokenException("Ticket " + ticketId + " is already set up for transfer!");
        }

        if (rfidService.isTicketLinked(ticketId)) {
            throw new TicketAlreadyLinkedException();
        }

        if (t.getOwner() != null && t.getOwner().getEmail().equals(receiverEmail)) {
            throw new TicketTransferTokenException("Cant send a ticket to yourself");
        }

        TicketTransferToken ttt = new TicketTransferToken(u, t);

        ttt = tttRepository.save(ttt);

        String acceptUrl = acceptTransferUrl + "?token=" + ttt.getToken();
        mailService.sendTicketTransferMail(ttt.getTicket().getOwner(), ttt.getUser(), acceptUrl);

        return ttt;
    }

    @Override
    public Ticket transferTicket(String token) {
        TicketTransferToken ttt = getTicketTransferTokenIfValid(token);
        Ticket t = ttt.getTicket();

        if (rfidService.isTicketLinked(t.getId())) {
            throw new TicketAlreadyLinkedException();
        }

        User newOwner = ttt.getUser();
        t.setOwner(newOwner);
        t = ticketRepository.save(t);

        ttt.use();
        tttRepository.save(ttt);

        return t;
    }

    @Override
    public Ticket cancelTicketTransfer(String token) {
        TicketTransferToken ttt = getTicketTransferTokenIfValid(token);
        ttt.revoke();
        tttRepository.save(ttt);
        return ttt.getTicket();
    }

    @Override
    public Collection<TicketTransferToken> getValidTicketTransferTokensByUserEmail(String email) {
        return tttRepository.findAllByTicketOwnerEmailIgnoreCase(email).stream().
                filter(TicketTransferToken::isValid).
                collect(Collectors.toList());
    }

    @Override
    public Collection<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Override
    public List<Ticket> getAllTicketsWithTransport() {
        TicketOption pickupServiceOption = getTicketOptionByName("pickupService");

        return ticketRepository.findAll().stream().
                filter(ticket -> ticket.getEnabledOptions().contains(pickupServiceOption)).
                collect(Collectors.toList());
    }

    @Override
    public Ticket assignTicketToUser(Long ticketId, String email) {
        Ticket ticket = getTicketById(ticketId);
        User user = userService.getUserByEmail(email);
        ticket.setOwner(user);
        return ticketRepository.save(ticket);
    }

    @Override
    public TicketType addTicketType(TicketType type) {
        return ticketTypeRepository.save(type);
    }

    @Override
    public Collection<TicketType> getAllTicketTypes() {
        return ticketTypeRepository.findAll();
    }

    @Override
    public TicketType updateTicketType(Long typeId, TicketType type) {
        if (typeId == null || !ticketTypeRepository.exists(typeId)) {
            throw new TicketTypeNotFoundException("with ID " + typeId);
        }
        type.setId(typeId);
        return ticketTypeRepository.save(type);
    }

    @Override
    public void deleteTicketType(Long typeId) {
        ticketTypeRepository.delete(typeId);
    }

    @Override
    public TicketOption addTicketOption(TicketOption option) {
        return ticketOptionRepository.save(option);
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
    public Collection<Ticket> getOwnedTicketsAndFromTeamMembers(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User can't be null");
        }

        Collection<Team> captainedTeams = teamService.getTeamByCaptainId(user.getId());

        if (captainedTeams.isEmpty()) {
            return ticketRepository.findAllByOwnerEmailIgnoreCase(user.getEmail());
        } else {
            return captainedTeams.stream().
                    // Get all team members, including the owner
                            flatMap(team -> team.getMembers().stream()).
                    // Find all tickets of those members and filter for validity
                            flatMap(
                            member -> ticketRepository.findAllByOwnerEmailIgnoreCase(member.getEmail()).stream()).
                            filter(Ticket::isValid).
                            collect(Collectors.toSet());
        }
    }
}
