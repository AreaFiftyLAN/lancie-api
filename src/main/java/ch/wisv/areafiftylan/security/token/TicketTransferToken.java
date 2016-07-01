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

package ch.wisv.areafiftylan.security.token;

import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.users.model.User;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Created by beer on 11-3-16.
 */
@Entity
public class TicketTransferToken extends Token {
    private static final int EXPIRATION = 0;

    @OneToOne(targetEntity = Ticket.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private Ticket ticket;

    public TicketTransferToken() {
    }

    public TicketTransferToken(User user, Ticket ticket) {
        super(user, EXPIRATION);
        this.ticket = ticket;
    }

    public Ticket getTicket() {
        return ticket;
    }
}
