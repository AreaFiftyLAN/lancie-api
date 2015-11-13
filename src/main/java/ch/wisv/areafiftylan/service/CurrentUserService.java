package ch.wisv.areafiftylan.service;

/**
 * The CurrentUserService is for permissions that require some logic to be determined.
 */
public interface CurrentUserService {
    boolean canAccessUser(Object principal, Long userId);
}