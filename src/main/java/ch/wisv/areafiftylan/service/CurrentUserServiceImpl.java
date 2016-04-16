package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.exception.TokenNotFoundException;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Role;
import ch.wisv.areafiftylan.security.token.TeamInviteToken;
import ch.wisv.areafiftylan.security.token.TicketTransferToken;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import ch.wisv.areafiftylan.service.repository.token.TicketTransferTokenRepository;
import ch.wisv.areafiftylan.service.repository.token.TeamInviteTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    private TeamService teamService;
    private OrderService orderService;
    private TicketRepository ticketRepository;
    private TicketService ticketService;
    private TeamInviteTokenRepository teamInviteTokenRepository;
    private TicketTransferTokenRepository tttRepository;

    @Autowired
    public CurrentUserServiceImpl(TeamService teamService, OrderService orderService, TicketRepository ticketRepository,
                                  TicketService ticketService, TeamInviteTokenRepository teamInviteTokenRepository,
                                  TicketTransferTokenRepository tttRepository) {
        this.teamService = teamService;
        this.orderService = orderService;
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
        this.teamInviteTokenRepository = teamInviteTokenRepository;
        this.tttRepository = tttRepository;
    }

    @Override
    public boolean canAccessUser(Object principal, Long userId) {
        if (principal instanceof UserDetails) {
            User user = (User) principal;
            return user.getId().equals(userId) || user.getAuthorities().contains(Role.ROLE_ADMIN);
        } else {
            return false;
        }
    }

    @Override
    public boolean canAccessTeam(Object principal, Long teamId) {
        if (principal instanceof UserDetails) {
            User user = (User) principal;
            Team team = teamService.getTeamById(teamId);
            // Check for each of the teammembers if the username matches the requester
            return team.getMembers().stream().anyMatch(u -> u.getUsername().equals(user.getUsername())) ||
                    user.getAuthorities().contains(Role.ROLE_ADMIN);
        } else {
            return false;
        }
    }

    @Override
    public boolean canEditTeam(Object principal, Long teamId) {
        if (principal instanceof UserDetails) {
            UserDetails user = (UserDetails) principal;
            Team team = teamService.getTeamById(teamId);
            return team.getCaptain().getUsername().equals(user.getUsername()) ||
                    user.getAuthorities().contains(Role.ROLE_ADMIN);
        } else {
            return false;
        }
    }

    @Override
    public boolean canRemoveFromTeam(Object principal, Long teamId, String username) {
        if (principal instanceof UserDetails) {
            UserDetails currentUser = (UserDetails) principal;
            Team team = teamService.getTeamById(teamId);

            // The Teamcaptain can't remove himself
            if (team.getCaptain().getUsername().equals(username)) {
                return false;
            }

            // You can remove people from a Team if you're Admin, the Team Captain, or if you want to remove yourself
            // from the Team
            return team.getCaptain().getUsername().equals(currentUser.getUsername()) ||
                    currentUser.getAuthorities().contains(Role.ROLE_ADMIN) ||
                    currentUser.getUsername().equals(username);
        } else {
            return false;
        }
    }

    @Override
    public boolean canAccessOrder(Object principal, Long orderId) {
        if (principal instanceof UserDetails) {
            User user = (User) principal;
            // Check for each of the teammembers if the username matches the requester
            return orderService.getOrderById(orderId).getUser().getUsername().equals(user.getUsername()) ||
                    user.getAuthorities().contains(Role.ROLE_ADMIN);
        } else {
            return false;
        }
    }

    @Override
    public boolean isTicketOwner(Object principal, Long ticketId) {
        if (principal instanceof UserDetails) {
            User user = (User) principal;
            return ticketService.getTicketById(ticketId).getOwner().equals(user);
        } else {
            return false;
        }
    }

    @Override
    public boolean canReserveSeat(Object principal, Long ticketId) {
        if (principal instanceof UserDetails) {
            User user = (User) principal;

            Ticket ticket = ticketRepository.findOne(ticketId);
            if (ticket.getOwner().equals(user) || user.getAuthorities().contains(Role.ROLE_ADMIN)) {
                return true;
            }

            Collection<Team> userTeams = teamService.getTeamByCaptainId(user.getId());

            // For each set of members in a team, check if the owner of the ticket is one of them.
            return userTeams.stream().map(Team::getMembers).anyMatch(members -> members.contains(ticket.getOwner()));
        } else {
            return false;
        }
    }

    @Override
    public boolean hasAnyTicket(Object principal) {
        if (principal instanceof UserDetails) {
            User user = (User) principal;
            return hasAnyTicket(user.getUsername()) || user.getAuthorities().contains(Role.ROLE_ADMIN);
        } else {
            return false;
        }
    }

    @Override
    public boolean hasAnyTicket(String username) {
        Optional<Ticket> ticket = ticketRepository.findByOwnerUsername(username);
        return ticket.isPresent() && ticket.get().isValid();
    }

    @Override
    public boolean canRevokeInvite(Object principal, String token) {
        if (principal instanceof UserDetails) {
            User user = (User) principal;

            TeamInviteToken teamInviteToken =
                    teamInviteTokenRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));

            // Tokens can be revoked by the target user, an Admin or the Captain
            return teamInviteToken.getUser().equals(user) || user.getAuthorities().contains(Role.ROLE_ADMIN) ||
                    teamInviteToken.getTeam().getCaptain().equals(user);
        } else {
            return false;
        }
    }

    @Override
    public boolean canAcceptInvite(Object principal, String token) {
        if (principal instanceof UserDetails) {
            User user = (User) principal;

            TeamInviteToken teamInviteToken =
                    teamInviteTokenRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));

            // Tokens can only be accepted by the target user
            return teamInviteToken.getUser().equals(user);
        } else {
            return false;
        }

    }

    @Override
    public boolean isTicketSender(Object principal, String token) {
        TicketTransferToken ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));

        if (principal instanceof UserDetails) {
            User user = (User) principal;
            return ttt.getTicket().getOwner().equals(user);
        } else {
            return false;
        }
    }

    @Override
    public boolean isTicketReceiver(Object principal, String token) {
        TicketTransferToken ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));

        if (principal instanceof UserDetails) {
            User user = (User) principal;
            return ttt.getUser().equals(user);
        } else {
            return false;
        }
    }
}
