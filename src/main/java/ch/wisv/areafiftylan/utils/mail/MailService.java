/*
 * Copyright (c) 2018  W.I.S.V. 'Christiaan Huygens'
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

package ch.wisv.areafiftylan.utils.mail;

import ch.wisv.areafiftylan.products.model.order.Order;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.users.model.User;

import java.util.Collection;

public interface MailService {

    void sendMail(String recipientEmail, String recipientName, String subject, String message);

    void sendContactMail(String senderEmail, String subject, String message);

    void sendTemplateMailToTeam(Team team, MailDTO mailDTO);

    void sendTemplateMailToAll(Collection<User> users, MailDTO mailDTO);

    void sendTemplateMailToUser(User user, MailDTO mailDTO);

    void sendVerificationmail(User user, String url);

    void sendOrderConfirmation(Order order);

    void sendPasswordResetMail(User user, String url);

    void sendTicketTransferMail(User sender, User receiver, String url);

    void sendTeamInviteMail(User user, String teamName, User teamCaptain);

    void sendSeatOverrideMail(User user);
}
