package ch.wisv.areafiftylan.unit;

import ch.wisv.areafiftylan.exception.*;
import ch.wisv.areafiftylan.extras.rfid.model.RFIDLink;
import ch.wisv.areafiftylan.extras.rfid.service.RFIDLinkRepository;
import ch.wisv.areafiftylan.extras.rfid.service.RFIDService;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.users.model.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RFIDServiceTest extends ServiceTest{

    @Autowired
    RFIDService rfidService;

    @Autowired
    RFIDLinkRepository rfidLinkRepository;

    private RFIDLink persistRFIDLink() {
        Ticket ticket = persistTicket();
        return persistRFIDLink(ticket);
    }

    private RFIDLink persistRFIDLink(Ticket ticket) {
        String rfid = String.format("%010d", rfidLinkRepository.count());
        return persistRFIDLink(rfid, ticket);
    }

    private RFIDLink persistRFIDLink(String rfid, Ticket ticket) {
        return testEntityManager.persist(new RFIDLink(rfid, ticket));
    }

    @Test
    public void getAllRFIDLinksTest() {
        RFIDLink link1 = persistRFIDLink();
        RFIDLink link2 = persistRFIDLink();

        Collection<RFIDLink> links = rfidService.getAllRFIDLinks();
        assertTrue(links.contains(link1));
        assertTrue(links.contains(link2));
    }

    @Test
    public void getTicketIdByRFIDTest() {
        User user = persistUser();
        Ticket ticket = persistTicketForUser(user);
        String rfid = "0000000001";
        rfidService.addRFIDLink(rfid, ticket.getId());
        assertEquals(ticket.getId(), rfidService.getTicketIdByRFID(rfid));
    }

    @Test
    public void getUserByRFIDTest() {
        User user = persistUser();
        Ticket ticket = persistTicketForUser(user);
        String rfid = "0000000001";
        rfidService.addRFIDLink(rfid, ticket.getId());
        assertEquals(user, rfidService.getUserByRFID(rfid));
    }

    @Test
    public void addRFIDLinkTest() {
        User user = persistUser();
        Ticket ticket = persistTicketForUser(user);
        String rfid = "0000000001";
        RFIDLink link = rfidService.addRFIDLink(rfid, ticket.getId());
        assertTrue(link.getRfid().equals(rfid));
        assertTrue(link.getTicket().equals(ticket));
    }

    @Test(expected = InvalidRFIDException.class)
    public void addRFIDLinkTooShortTest() {
        User user = persistUser();
        Ticket ticket = persistTicketForUser(user);
        String rfid = "0000001";
        rfidService.addRFIDLink(rfid, ticket.getId());
        assertEquals(0, rfidLinkRepository.findAll().size());
    }

    @Test(expected = InvalidRFIDException.class)
    public void addRFIDLinkTooLongTest() {
        User user = persistUser();
        Ticket ticket = persistTicketForUser(user);
        String rfid = "0000000000000000001";
        rfidService.addRFIDLink(rfid, ticket.getId());
        assertEquals(0, rfidLinkRepository.findAll().size());
    }

    @Test(expected = RFIDTakenException.class)
    public void addRFIDLinkAlreadyTakenTest() {
        User user = persistUser();
        Ticket ticket = persistTicketForUser(user);
        String rfid = "0000000001";
        rfidService.addRFIDLink(rfid, ticket.getId());
        rfidService.addRFIDLink(rfid, ticket.getId());
        assertEquals(0, rfidLinkRepository.findAll().size());
    }

    @Test(expected = InvalidTicketException.class)
    public void addRFIDLinkInvalidTicketTest() {
        User user = persistUser();
        Ticket ticket = persistTicketForUser(user);
        ticket.setValid(false);
        ticketRepository.save(ticket);
        String rfid = "0000000001";
        rfidService.addRFIDLink(rfid, ticket.getId());
        assertEquals(0, rfidLinkRepository.findAll().size());
    }

    @Test(expected = TicketAlreadyLinkedException.class)
    public void addRFIDLinkTicketAlreadyLinkedTest() {
        User user = persistUser();
        Ticket ticket = persistTicketForUser(user);
        String rfid1 = "0000000001";
        String rfid2 = "0000000002";
        rfidService.addRFIDLink(rfid1, ticket.getId());
        rfidService.addRFIDLink(rfid2, ticket.getId());
        assertEquals(0, rfidLinkRepository.findAll().size());
    }

    @Test
    public void removeRFIDLinkByRfidTest() {
        User user = persistUser();
        Ticket ticket = persistTicketForUser(user);
        String rfid = "0000000001";
        RFIDLink link = new RFIDLink(rfid, ticket);
        rfidLinkRepository.save(link);
        assertEquals(1, rfidLinkRepository.findAll().size());
        rfidService.removeRFIDLink(rfid);
        assertEquals(0, rfidLinkRepository.findAll().size());
    }

    @Test
    public void removeRFIDLinkByTicketIdTest() {
        User user = persistUser();
        Ticket ticket = persistTicketForUser(user);
        String rfid = "0000000001";
        RFIDLink link = new RFIDLink(rfid, ticket);
        rfidLinkRepository.save(link);
        assertEquals(1, rfidLinkRepository.findAll().size());
        rfidService.removeRFIDLink(ticket.getId());
        assertEquals(0, rfidLinkRepository.findAll().size());
    }

    @Test(expected = RFIDNotFoundException.class)
    public void removeRFIDLinkNotThereTest() {
        String rfid = "0000000001";
        assertEquals(0, rfidLinkRepository.findAll().size());
        rfidService.removeRFIDLink(rfid);
    }

    @Test
    public void isTicketLinkedTest() throws Exception {
        User user = persistUser();
        Ticket ticket = persistTicketForUser(user);
        String rfid = "0000000001";
        rfidService.addRFIDLink(rfid, ticket.getId());
        assertTrue(rfidService.isTicketLinked(ticket.getId()));
    }

    @Test
    public void isTicketLinkedNotLinkedTest() throws Exception {
        User user = persistUser();
        Ticket ticket = persistTicketForUser(user);
        assertFalse(rfidService.isTicketLinked(ticket.getId()));
    }

    @Test
    public void isOwnerLinkedTest() throws Exception {
        User user = persistUser();
        Ticket ticket = persistTicketForUser(user);
        String rfid = "0000000001";
        rfidService.addRFIDLink(rfid, ticket.getId());
        assertTrue(rfidService.isOwnerLinked(user.getEmail()));
    }

    @Test
    public void isOwnerLinkedNotLinkedTest() throws Exception {
        User user = persistUser();
        persistTicketForUser(user);
        assertFalse(rfidService.isOwnerLinked(user.getEmail()));
    }
}