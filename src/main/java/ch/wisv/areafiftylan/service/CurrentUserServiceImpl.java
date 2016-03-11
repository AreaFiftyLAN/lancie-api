package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Role;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    @Autowired
    TeamService teamService;

    @Autowired
    OrderService orderService;

    @Autowired
    TicketRepository ticketRepository;

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
            Team team = teamService.getTeamById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId));

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
    public boolean hasTicket(Object principal) {
        if (principal instanceof UserDetails) {
            User user = (User) principal;
            return hasTicket(user.getUsername()) || user.getAuthorities().contains(Role.ROLE_ADMIN);
        } else {
            return false;
        }
    }

    @Override
    public boolean hasTicket(String username) {
        return ticketRepository.findByOwnerUsername(username).isPresent();
    }
}
