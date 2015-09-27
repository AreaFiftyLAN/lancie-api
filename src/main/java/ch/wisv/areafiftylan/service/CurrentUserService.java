package ch.wisv.areafiftylan.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface CurrentUserService {
    boolean canAccessUser(UserDetails currentUser, Long userId);
}