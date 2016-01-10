package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Role;
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
    public boolean canReserveSeat(Object principal, String username) {
        if (principal instanceof UserDetails) {
            User user = (User) principal;

            Collection<Team> userTeams = teamService.getTeamByCaptainId(user.getId());


            // This really intense lambda experssion gets all members from the previously selected teams. It filters
            // them on the given username. If the given username is present in one of the captain's team, the count
            // is higher then 0, so the operation is allowed.
            return userTeams.stream().map(Team::getMembers).filter(users -> user.getUsername().equals(username))
                    .count() > 0 || user.getAuthorities().contains(Role.ROLE_ADMIN) ||
                    user.getUsername().equals(username);

        } else {
            return false;
        }
    }
}
