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
    public Long getTicketIdByRFID(String rfid) {
        return getLinkByRFID(rfid).getTicket().getId();
    }

    @Override
    public String getRFIDByTicketId(Long ticketId) {
        return getLinkByTicketId(ticketId).getRFID();
    }

    @Override
    public boolean isRFIDUsed(String rfid) {
        return rfidLinkRepository.findByRfid(rfid).isPresent();
    }

    @Override
    public void addRFIDLink(RFIDLink link) {
        rfidLinkRepository.saveAndFlush(link);
    }

    public RFIDLink getLinkByRFID(String rfid) {
        return rfidLinkRepository.findByRfid(rfid)
                .orElseThrow(() -> new RFIDNotFoundException());
    }

    public RFIDLink getLinkByTicketId(Long ticketId) {
        return rfidLinkRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new RFIDNotFoundException());
    }
}
