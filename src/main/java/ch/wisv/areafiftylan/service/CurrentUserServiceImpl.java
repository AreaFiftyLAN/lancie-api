package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.util.Role;
import ch.wisv.areafiftylan.security.CurrentUser;

public class CurrentUserServiceImpl implements CurrentUserService {
    @Override
    public boolean canAccessUser(CurrentUser currentUser, Long userId) {
        return currentUser != null &&
                (currentUser.getRoles().contains(Role.ADMIN) || currentUser.getId().equals(userId));
    }
}
