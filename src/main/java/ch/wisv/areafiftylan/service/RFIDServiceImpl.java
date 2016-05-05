package ch.wisv.areafiftylan.service;

import ch.wisv.areafiftylan.exception.RFIDNotFoundException;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.relations.RFIDLink;
import ch.wisv.areafiftylan.service.repository.RFIDLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Created by beer on 5-5-16.
 */
@Service
public class RFIDServiceImpl implements RFIDService{
    @Autowired
    private RFIDLinkRepository rfidLinkRepository;

    @Override
    public Collection<RFIDLink> getAllRFIDLinks() {
        return rfidLinkRepository.findAll();
    }

    @Override
    public RFIDLink getLinkByRFID(String rfid) {
        return rfidLinkRepository.findByRFID(rfid)
                .orElseThrow(() -> new RFIDNotFoundException());
    }

    @Override
    public Ticket getTicketByRFID(String rfid) {
        return getLinkByRFID(rfid).getTicket();
    }

    @Override
    public String getRFIDByTicket(Ticket ticket) {
        return rfidLinkRepository.findByTicket(ticket)
                .orElseThrow(() -> new RFIDNotFoundException())
                .getRFID();
    }

    @Override
    public boolean isRFIDUsed(String rfid) {
        return rfidLinkRepository.findByRFID(rfid).isPresent();
    }

    @Override
    public void addRFIDLink(RFIDLink link) {
        rfidLinkRepository.saveAndFlush(link);
    }
}
