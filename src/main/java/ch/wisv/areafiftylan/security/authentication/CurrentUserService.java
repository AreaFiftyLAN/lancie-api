/*
 * Copyright (c) 2016  W.I.S.V. 'Christiaan Huygens'
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.wisv.areafiftylan.security.authentication;

/**
 * The CurrentUserService is for permissions that require some logic to be determined.
 */
public interface CurrentUserService {
    boolean canAccessUser(Object principal, Long userId);

    boolean canAccessTeam(Object principal, Long teamId);

    boolean canEditTeam(Object principal, Long teamId);

    boolean canRemoveFromTeam(Object principal, Long teamId, String email);

    boolean canAccessOrder(Object principal, Long orderId);

    boolean isTicketOwner(Object principal, Long ticketId);

    boolean isTicketSender(Object principal, String token);

    boolean isTicketReceiver(Object principal, String token);

    boolean canReserveSeat(Object principal, Long ticketId);

    boolean hasAnyTicket(Object principal);

    boolean hasAnyTicket(String email);

    boolean canRevokeInvite(Object principal, String token);

    boolean canAcceptInvite(Object principal, String token);
}