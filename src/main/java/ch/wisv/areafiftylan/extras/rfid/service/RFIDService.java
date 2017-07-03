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

package ch.wisv.areafiftylan.extras.rfid.service;

import ch.wisv.areafiftylan.extras.rfid.model.RFIDLink;
import ch.wisv.areafiftylan.users.model.User;

import java.util.Collection;

public interface RFIDService {
    Collection<RFIDLink> getAllRFIDLinks();

    Long getTicketIdByRFID(String rfid);

    User getUserByRFID(String rfid);

    void addRFIDLink(String rfid, Long ticketId);

    RFIDLink removeRFIDLink(String rfid);

    RFIDLink removeRFIDLink(Long ticketId);

    boolean isTicketLinked(Long ticketId);
}
