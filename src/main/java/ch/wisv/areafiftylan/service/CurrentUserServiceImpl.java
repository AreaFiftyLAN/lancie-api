package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.exception.TeamNotFoundException;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

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
            Team team = teamService.getTeamById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId));
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
            Team team = teamService.getTeamById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId));
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
}
