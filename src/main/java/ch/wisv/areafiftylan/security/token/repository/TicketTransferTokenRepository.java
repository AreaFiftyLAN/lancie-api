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

package ch.wisv.areafiftylan.security.token.repository;

import ch.wisv.areafiftylan.security.token.TicketTransferToken;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface TicketTransferTokenRepository extends TokenRepository<TicketTransferToken> {

    Collection<TicketTransferToken> findAllByTicketId(Long ticketId);

    Collection<TicketTransferToken> findAllByTicketOwnerUsernameIgnoreCase(String username);
}
