package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.relations.RFIDLink;

import java.util.Collection;

/**
 * Created by beer on 5-5-16.
 */
public interface RFIDService {
    Collection<RFIDLink> getAllRFIDLinks();

    RFIDLink getLinkByRFID(String rfid);

    Ticket getTicketByRFID(String rfid);

    String getRFIDByTicket(Ticket ticket);

    boolean isRFIDUsed(String rfid);

    void addRFIDLink(RFIDLink link);
}
