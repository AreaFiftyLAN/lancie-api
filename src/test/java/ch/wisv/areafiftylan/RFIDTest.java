package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.relations.RFIDLink;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.service.repository.RFIDLinkRepository;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import ch.wisv.areafiftylan.util.SessionData;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
    private RFIDLink link;


    private Ticket ticket;

    @Before
    public void RFIDTestInit(){
        ticket = makeTicket();
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

        Ticket ticket2 = makeTicket();
        ticket2 = ticketRepository.saveAndFlush(ticket2);

        RFIDLink link2 = new RFIDLink("1234567890", ticket2);
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
}
