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

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;

import static org.hamcrest.Matchers.*;

/**
 * Created by beer on 7-5-16.
 */
public class RFIDTest extends IntegrationTest {
    private final String RFID_ENDPOINT = "/rfid";

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
    public void testGetRFIDLink_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given()
                .filter(sessionFilter)
                .header(session.getCsrfHeader())
        .when()
                .get(RFID_ENDPOINT + "/" + LINK_RFID + "/ticketId")
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString(link.getTicket().getId().toString()));
    }

    @Test
    public void testGetRFIDLink_InvalidRFID_Admin(){
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
    public void testGetRFIDLink_UnusedRFID_Admin(){
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

        RFIDLink madeLink = rfidLinkRepository.findByRfid(UNUSED_RFID).orElse(null);
        Assert.assertNotNull(madeLink);
        Assert.assertEquals(madeLink.getTicket().getId(), otherTicket.getId());
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

        RFIDLink madeLink = rfidLinkRepository.findByRfid(INVALID_RFID).orElse(null);
        Assert.assertNull(madeLink);
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

        RFIDLink madeLink = rfidLinkRepository.findByRfid(LINK_RFID).orElse(null);
        Assert.assertNotNull(madeLink);
        Assert.assertEquals(madeLink.getTicket().getId(), ticket.getId());
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

        RFIDLink madeLink = rfidLinkRepository.findByRfid(UNUSED_RFID).orElse(null);
        Assert.assertNull(madeLink);
    }

    @Test
    public void testAddRFIDLink_TicketDoesntExist_Admin(){
        Long unusedTicketId = 1260L;

        //Just to make sure the id is truly unused
        while(unusedTicketId == ticket.getId() || unusedTicketId == otherTicket.getId()){
            unusedTicketId += 1;
        }

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

        RFIDLink madeLink = rfidLinkRepository.findByRfid(UNUSED_RFID).orElse(null);
        Assert.assertNull(madeLink);
    }

    private HashMap<String, String> makeRFIDLinkDTO(String rfid, Long ticketId){
        HashMap<String, String> rfidLinkDTO = new HashMap<>();
        rfidLinkDTO.put("rfid", rfid);
        rfidLinkDTO.put("ticketId", ticketId.toString());

        return rfidLinkDTO;
    }

    //TODO: Write tests for the /tickets/{ticketId}/rfid endpoint
}
