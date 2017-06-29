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

package ch.wisv.areafiftylan.extras.rfid.model;

import ch.wisv.areafiftylan.exception.InvalidRFIDException;
import ch.wisv.areafiftylan.products.model.Ticket;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class RFIDLink {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String rfid;

    @NonNull
    @OneToOne(cascade = CascadeType.MERGE)
    private Ticket ticket;


    public RFIDLink(String rfid, Ticket ticket) {
        if (isInvalidRFID(rfid)) {
            throw new InvalidRFIDException(rfid);
        }

        this.rfid = rfid;
        this.ticket = ticket;
    }

    //Static Content
    private static final int RFID_CHAR_COUNT = 10;

    public static boolean isInvalidRFID(String rfid) {
        return rfid.length() != RFID_CHAR_COUNT;
    }
}
