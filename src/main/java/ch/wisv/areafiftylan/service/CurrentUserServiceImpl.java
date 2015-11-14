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

    @Override
    public boolean canAccessUser(Object principal, Long userId) {

        if (principal instanceof UserDetails) {
            User user = (User) principal;
            return user.getId().equals(userId) || user.getAuthorities().contains(Role.ADMIN);
        } else {
            return false;
        }


    }

    @Override
    public boolean canAccessTeam(Object principal, Long teamId) {

        if (principal instanceof UserDetails) {
            User user = (User) principal;
            Team team = teamService.getTeamById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId));
            return team.getMembers().contains(user) || user.getAuthorities().contains(Role.ADMIN);
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
                    user.getAuthorities().contains(Role.ADMIN);
        } else {
            return false;
        }
    }
}
