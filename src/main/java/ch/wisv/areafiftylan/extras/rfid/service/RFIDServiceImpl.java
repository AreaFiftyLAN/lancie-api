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

package ch.wisv.areafiftylan.extras.rfid.service;

import ch.wisv.areafiftylan.exception.*;
import ch.wisv.areafiftylan.extras.rfid.model.RFIDLink;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.service.repository.TicketRepository;
import ch.wisv.areafiftylan.users.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class RFIDServiceImpl implements RFIDService {

    public static final int RFID_CHAR_COUNT = 10;

    private final RFIDLinkRepository rfidLinkRepository;
    private final TicketRepository ticketRepository;

    @Autowired
    public RFIDServiceImpl(RFIDLinkRepository rfidLinkRepository, TicketRepository ticketRepository) {
        this.rfidLinkRepository = rfidLinkRepository;
        this.ticketRepository = ticketRepository;
    }

    private Ticket getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId).
                orElseThrow(TicketNotFoundException::new);
    }

    @Override
    public Collection<RFIDLink> getAllRFIDLinks() {
        return rfidLinkRepository.findAll();
    }

    @Override
    public Long getTicketIdByRFID(String rfid) {
        return getLinkByRFID(rfid).getTicket().getId();
    }

    @Override
    public User getUserByRFID(String rfid) {
        return getLinkByRFID(rfid).getTicket().getOwner();
    }

    @Override
    public RFIDLink addRFIDLink(String rfid, Long ticketId) {
        if (!isValidRfid(rfid)) {
            throw new InvalidRFIDException(rfid);
        }
        if (isUsedRfid(rfid)) {
            throw new RFIDTakenException(rfid);
        }
        if (!isValidTicket(ticketId)) {
            throw new InvalidTicketException("Can't link ticket to RFID; ticket is invalid.");
        }
        if (isTicketLinked(ticketId)) {
            throw new TicketAlreadyLinkedException();
        }

        Ticket ticket = getTicketById(ticketId);
        RFIDLink newLink = new RFIDLink(rfid, ticket);
        return rfidLinkRepository.saveAndFlush(newLink);
    }

    @Override
    public RFIDLink removeRFIDLink(String rfid) {
        RFIDLink link = getLinkByRFID(rfid);
        rfidLinkRepository.delete(link);
        return link;
    }

    @Override
    public RFIDLink removeRFIDLink(Long ticketId) {
        RFIDLink link = getLinkByTicketId(ticketId);
        rfidLinkRepository.delete(link);
        return link;
    }

    @Override
    public boolean isTicketLinked(Long ticketId) {
        return rfidLinkRepository.findByTicketId(ticketId).isPresent();
    }

    private boolean isValidRfid(String rfid) {
        return rfid.length() == RFID_CHAR_COUNT;
    }

    private boolean isUsedRfid(String rfid) {
        return rfidLinkRepository.findByRfid(rfid).isPresent();
    }

    private boolean isValidTicket(Long ticketId) {
        return getTicketById(ticketId).isValid();
    }

    private RFIDLink getLinkByRFID(String rfid) {
        return rfidLinkRepository.findByRfid(rfid).orElseThrow(RFIDNotFoundException::new);
    }

    private RFIDLink getLinkByTicketId(Long ticketId) {
        return rfidLinkRepository.findByTicketId(ticketId).orElseThrow(RFIDNotFoundException::new);
    }
}
