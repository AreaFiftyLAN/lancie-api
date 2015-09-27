package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.util.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

public class CurrentUserServiceImpl implements CurrentUserService {

    @Autowired
    UserService userService;

    @Override
    public boolean canAccessUser(UserDetails currentUser, Long userId) {
        return currentUser != null && (currentUser.getAuthorities().contains(Role.ADMIN) ||
                userService.getUserByUsername(currentUser.getUsername()).get().getId().equals(userId));
    }
}
