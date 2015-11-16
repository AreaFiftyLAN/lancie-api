package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Role;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    @Override
    public boolean canAccessUser(Object principal, Long userId) {

        if (principal instanceof UserDetails) {
            User user = (User) principal;
            return user.getId().equals(userId) || user.getAuthorities().contains(Role.ADMIN);
        } else {
            return false;
        }


    }
}
