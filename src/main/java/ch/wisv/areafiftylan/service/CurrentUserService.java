package ch.wisv.areafiftylan.service;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * The CurrentUserService is for permissions that require some logic to be determined.
 */
public interface CurrentUserService {
    boolean canAccessUser(Object principal, Long userId);

    boolean canAccessTeam(Object principal, Long teamId);

    boolean canEditTeam(Object principal, Long teamId);
}