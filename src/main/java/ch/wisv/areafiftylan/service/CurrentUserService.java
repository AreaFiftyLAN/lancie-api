package ch.wisv.areafiftylan.service;

/**
 * The CurrentUserService is for permissions that require some logic to be determined.
 */
public interface CurrentUserService {
    boolean canAccessUser(Object principal, Long userId);

    boolean canAccessTeam(Object principal, Long teamId);

    boolean canEditTeam(Object principal, Long teamId);

    boolean canRemoveFromTeam(Object principal, Long teamId, String username);

    boolean canAccessOrder(Object principal, Long orderId);

    boolean canReserveSeat(Object principal, Long ticketId);

    boolean hasTicket(Object principal);

    boolean hasTicket(String username);

    boolean canRevokeInvite(Object principal, String token);

    boolean canAcceptInvite(Object principal, String token);
}