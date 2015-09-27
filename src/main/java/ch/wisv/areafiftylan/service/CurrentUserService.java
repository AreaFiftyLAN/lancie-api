package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.security.CurrentUser;

public interface CurrentUserService {
    boolean canAccessUser(CurrentUser currentUser, Long userId);
}