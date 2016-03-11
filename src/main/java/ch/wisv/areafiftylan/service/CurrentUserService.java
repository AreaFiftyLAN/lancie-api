package ch.wisv.areafiftylan.service;

import java.util.Objects;

/**
 * The CurrentUserService is for permissions that require some logic to be determined.
 */
public interface CurrentUserService {
    boolean canAccessUser(Object principal, Long userId);

    boolean canAccessTeam(Object principal, Long teamId);

    boolean canEditTeam(Object principal, Long teamId);

    boolean canRemoveFromTeam(Object principal, Long teamId, String username);

    boolean canAccessOrder(Object principal, Long orderId);

    boolean isTicketOwner(Object principal, Long ticketId);

    boolean isTicketSender(Object principal, String token);

    boolean isTicketReceiver(Object principal, String token);

    boolean canReserveSeat(Object principal, Long ticketId);

    boolean hasAnyTicket(Object principal);

    boolean hasAnyTicket(String username);

    boolean canRevokeInvite(Object principal, String token);

    boolean canAcceptInvite(Object principal, String token);
}