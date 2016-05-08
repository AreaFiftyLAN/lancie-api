package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.exception.*;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.relations.RFIDLink;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.service.repository.RFIDLinkRepository;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import ch.wisv.areafiftylan.util.SessionData;
import com.jayway.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;

import static org.hamcrest.Matchers.*;

/**
 * Created by beer on 7-5-16.
 */
public class RFIDTest extends IntegrationTest {
    private final String RFID_ENDPOINT = "/rfid";
    private final String RFID_TICKET_ENDPOINT = RFID_ENDPOINT + "/tickets";

    @Autowired
    private RFIDLinkRepository rfidLinkRepository;

    @Autowired
    private TicketRepository ticketRepository;

    private final String LINK_RFID = "0123456789";
    private final String UNUSED_RFID = "1212121212";
    private final String INVALID_RFID = "607";

    private RFIDLink link;
    private Ticket ticket;
    private Ticket otherTicket;

    @Before
    public void RFIDTestInit(){
        ticket = makeTicket();
        otherTicket = makeTicket();
        link = makeRFIDLink();
    }

    private Ticket makeTicket(){
        Ticket t = new Ticket(user, TicketType.EARLY_FULL, false, false);
        t = ticketRepository.saveAndFlush(t);

        return t;
    }

    private RFIDLink makeRFIDLink(){
        RFIDLink l = new RFIDLink(LINK_RFID, ticket);
        l = rfidLinkRepository.saveAndFlush(l);

        return l;
    }

    @After
    public void RFIDTestCleanup(){
        rfidLinkRepository.deleteAll();
        ticketRepository.deleteAll();
    }

    @Test
    public void testGetRFIDLinks_Anon(){
        when()
                .get(RFID_ENDPOINT)
        .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void testGetRFIDLinks_User(){
        SessionData session = login(user.getUsername(), userCleartextPassword);

        given()
                .filter(sessionFilter)
                .header(session.getCsrfHeader())
        .when()
                .get(RFID_ENDPOINT)
        .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void testGetRFIDLinksNone_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        rfidLinkRepository.delete(link);

        given()
                .filter(sessionFilter)
                .header(session.getCsrfHeader())
        .when()
                .get(RFID_ENDPOINT)
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$",  hasSize(0));
    }

    @Test
    public void testGetRFIDLinksSingle_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given()
                .filter(sessionFilter)
                .header(session.getCsrfHeader())
        .when()
                .get(RFID_ENDPOINT)
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$", hasSize(1))
                .body("ticket.id", containsInAnyOrder(link.getTicket().getId().intValue()));
    }

    @Test
    public void testGetRFIDLinksMultiple_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        RFIDLink link2 = new RFIDLink("1234567890", otherTicket);
        link2 = rfidLinkRepository.saveAndFlush(link2);

        given()
                .filter(sessionFilter)
                .header(session.getCsrfHeader())
        .when()
                .get(RFID_ENDPOINT)
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$", hasSize(2))
                .body("ticket.id", hasItems(link.getTicket().getId().intValue(), link2.getTicket().getId().intValue()));
    }

    @Test
    public void testGetTicketIdByRFID_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given()
                .filter(sessionFilter)
                .header(session.getCsrfHeader())
        .when()
                .get(RFID_ENDPOINT + "/" + LINK_RFID + "/ticketId")
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo(ticket.getId().toString()));
    }

    @Test
    public void testGetTicketId_InvalidRFID_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        InvalidRFIDException e = new InvalidRFIDException(INVALID_RFID);

        given()
                .filter(sessionFilter)
                .header(session.getCsrfHeader())
        .when()
                .get(RFID_ENDPOINT + "/" + INVALID_RFID + "/ticketId")
        .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(containsString(e.getMessage()));
    }

    @Test
    public void testGetTicketId_UnusedRFID_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        RFIDNotFoundException e = new RFIDNotFoundException();

        given()
                .filter(sessionFilter)
                .header(session.getCsrfHeader())
        .when()
                .get(RFID_ENDPOINT + "/" + UNUSED_RFID + "/ticketId")
        .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(containsString(e.getMessage()));
    }

    @Test
    public void testAddRFIDLink_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given()
                .filter(sessionFilter)
                .header(session.getCsrfHeader())
        .when()
                .content(makeRFIDLinkDTO(UNUSED_RFID, otherTicket.getId()))
                .contentType(ContentType.JSON)
                .post(RFID_ENDPOINT)
        .then()
                .statusCode(HttpStatus.SC_OK);

        Optional<RFIDLink> queryResult = rfidLinkRepository.findByRfid(UNUSED_RFID);
        Assert.assertTrue(queryResult.isPresent());
        Assert.assertEquals(queryResult.get().getTicket().getId(), otherTicket.getId());
    }

    @Test
    public void testAddRFIDLink_InvalidRFID_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        InvalidRFIDException e = new InvalidRFIDException(INVALID_RFID);

        given()
                .filter(sessionFilter)
                .header(session.getCsrfHeader())
        .when()
                .content(makeRFIDLinkDTO(INVALID_RFID, otherTicket.getId()))
                .contentType(ContentType.JSON)
                .post(RFID_ENDPOINT)
        .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(containsString(e.getMessage()));

        Assert.assertFalse(rfidLinkRepository.findByRfid(INVALID_RFID).isPresent());
    }

    @Test
    public void testAddRFIDLink_RFIDTaken_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        RFIDTakenException e = new RFIDTakenException(LINK_RFID);

        given()
                .filter(sessionFilter)
                .header(session.getCsrfHeader())
        .when()
                .content(makeRFIDLinkDTO(LINK_RFID, otherTicket.getId()))
                .contentType(ContentType.JSON)
                .post(RFID_ENDPOINT)
        .then()
                .statusCode(HttpStatus.SC_CONFLICT)
                .body(containsString(e.getMessage()));

        Optional<RFIDLink> queryResult = rfidLinkRepository.findByRfid(LINK_RFID);
        Assert.assertTrue(queryResult.isPresent());
        Assert.assertEquals(queryResult.get().getTicket().getId(), ticket.getId());
    }

    @Test
    public void testAddRFIDLink_TicketTaken_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        TicketAlreadyLinkedException e = new TicketAlreadyLinkedException();

        given()
                .filter(sessionFilter)
                .header(session.getCsrfHeader())
        .when()
                .content(makeRFIDLinkDTO(UNUSED_RFID, ticket.getId()))
                .contentType(ContentType.JSON)
                .post(RFID_ENDPOINT)
        .then()
                .statusCode(HttpStatus.SC_CONFLICT)
                .body(containsString(e.getMessage()));

        Assert.assertFalse(rfidLinkRepository.findByRfid(UNUSED_RFID).isPresent());
    }

    @Test
    public void testAddRFIDLink_TicketDoesntExist_Admin(){
        Long unusedTicketId = getUnusedTicketId();

        TicketNotFoundException e = new TicketNotFoundException();

        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given()
                .filter(sessionFilter)
                .header(session.getCsrfHeader())
        .when()
                .content(makeRFIDLinkDTO(UNUSED_RFID, unusedTicketId))
                .contentType(ContentType.JSON)
                .post(RFID_ENDPOINT)
        .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(containsString(e.getMessage()));

        Assert.assertFalse(rfidLinkRepository.findByRfid(UNUSED_RFID).isPresent());
    }

    @Test
    public void testRemoveRFIDLinkByRFID_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given()
                .filter(sessionFilter)
                .header(session.getCsrfHeader())
        .when()
                .delete(RFID_ENDPOINT + "/" + LINK_RFID)
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("ticket.id", equalTo(ticket.getId().intValue()));

        Assert.assertFalse(rfidLinkRepository.findByRfid(LINK_RFID).isPresent());
    }

    @Test
    public void testRemoveRFIDLinkByTicketId_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given()
                .filter(sessionFilter)
                .header(session.getCsrfHeader())
        .when()
                .delete(RFID_TICKET_ENDPOINT + "/" + ticket.getId())
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("ticket.id", equalTo(ticket.getId().intValue()));

        Assert.assertFalse(rfidLinkRepository.findByRfid(LINK_RFID).isPresent());
    }

    @Test
    public void testRemoveRFIDLink_LinkNotFound_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given()
                .filter(sessionFilter)
                .header(session.getCsrfHeader())
        .when()
                .delete(RFID_ENDPOINT + "/" + UNUSED_RFID)
        .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);

        Assert.assertTrue(rfidLinkRepository.findByRfid(LINK_RFID).isPresent());
    }

    @Test
    public void testRemoveRFIDLink_InvalidRFID_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        InvalidRFIDException e = new InvalidRFIDException(INVALID_RFID);

        given()
                .filter(sessionFilter)
                .header(session.getCsrfHeader())
        .when()
                .delete(RFID_ENDPOINT + "/" + INVALID_RFID)
        .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(containsString(e.getMessage()));
    }

    private HashMap<String, String> makeRFIDLinkDTO(String rfid, Long ticketId){
        HashMap<String, String> rfidLinkDTO = new HashMap<>();
        rfidLinkDTO.put("rfid", rfid);
        rfidLinkDTO.put("ticketId", ticketId.toString());

        return rfidLinkDTO;
    }

    private Long getUnusedTicketId(){
        Long unusedTicketId = 1260L;

        //Just to make sure the id is truly unused
        while(unusedTicketId == ticket.getId() || unusedTicketId == otherTicket.getId()){
            unusedTicketId += 1;
        }

        return unusedTicketId;
    }
}
