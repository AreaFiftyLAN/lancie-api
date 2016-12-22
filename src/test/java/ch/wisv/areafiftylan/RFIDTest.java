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


package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.exception.RFIDNotFoundException;
import ch.wisv.areafiftylan.extras.rfid.model.RFIDLink;
import ch.wisv.areafiftylan.extras.rfid.model.RFIDLinkRepository;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.service.repository.TicketRepository;
import ch.wisv.areafiftylan.users.model.User;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

public class RFIDTest extends XAuthIntegrationTest {
    private final String RFID_ENDPOINT = "/rfid";
    private final String RFID_TICKET_ENDPOINT = RFID_ENDPOINT + "/tickets";

    @Autowired
    private RFIDLinkRepository rfidLinkRepository;

    @Autowired
    private TicketRepository ticketRepository;

    private RFIDLink createRfidLink(Ticket ticket) {
        long rfid = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        RFIDLink l = new RFIDLink(String.valueOf(rfid), ticket);
        l = rfidLinkRepository.saveAndFlush(l);
        return l;
    }

    @Test
    public void testGetRFIDLinks_Anon() {
        //@formatter:off
        when()
            .get(RFID_ENDPOINT)
        .then()
            .statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetRFIDLinksAsUser() {
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .get(RFID_ENDPOINT).
        then()
            .statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetRFIDLinksNoneAsAdmin() {
        User user = createUser(true);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .get(RFID_ENDPOINT).
        then()
            .statusCode(HttpStatus.SC_OK)
            .body("$",  hasSize(Long.valueOf(rfidLinkRepository.count()).intValue()));
        //@formatter:on
    }

    @Test
    public void testGetRFIDLinksSingleAsAdmin() {
        User admin = createUser(true);
        Ticket ticket = createTicketForUser(admin);
        RFIDLink rfidLink = createRfidLink(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when()
            .get(RFID_ENDPOINT).
        then()
            .statusCode(HttpStatus.SC_OK)
            .body("$", hasSize(Long.valueOf(rfidLinkRepository.count()).intValue()))
            .body("ticket.id", hasItems(rfidLink.getTicket().getId().intValue()));
        //@formatter:on
    }

    @Test
    public void testGetRFIDLinksMultipleAsAdmin() {
        User admin = createUser(true);
        User user = createUser();

        Ticket ticket1 = createTicketForUser(admin);
        Ticket ticket2 = createTicketForUser(user);

        RFIDLink link = createRfidLink(ticket1);
        RFIDLink link2 = createRfidLink(ticket2);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when()
            .get(RFID_ENDPOINT).
        then()
            .statusCode(HttpStatus.SC_OK)
            .body("$", hasSize(Long.valueOf(rfidLinkRepository.count()).intValue()))
            .body("ticket.id", hasItems(link.getTicket().getId().intValue(), link2.getTicket().getId().intValue()));
        //@formatter:on
    }

    @Test
    public void testGetTicketIdByRFIDAsAdmin() {
        User user = createUser(true);
        Ticket ticket = createTicketForUser(user);
        RFIDLink link = createRfidLink(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .get(RFID_ENDPOINT + "/" + link.getRFID() + "/ticketId").
        then()
            .statusCode(HttpStatus.SC_OK)
            .body(equalTo(ticket.getId().toString()));
        //@formatter:on
    }

    @Test
    public void testGetTicketIdInvalidRFIDAsAdmin() {
        User user = createUser(true);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .get(RFID_ENDPOINT + "/123" + "/ticketId").
        then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testGetTicketIdUnusedRFIDAsAdmin() {
        User user = createUser(true);
        Ticket ticket = createTicketForUser(user);
        RFIDLink link = createRfidLink(ticket);
        String unused = String.valueOf(Long.valueOf(link.getRFID()) + 1);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .get(RFID_ENDPOINT + "/" + unused + "/ticketId").
        then()
            .statusCode(HttpStatus.SC_NOT_FOUND);
        //@formatter:on
    }

    @Test
    public void testAddRFIDLinkAsAdmin() {
        User user = createUser(true);
        Ticket ticket = createTicketForUser(user);
        HashMap<String, String> rfidLinkDTO = makeRFIDLinkDTO(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .body(rfidLinkDTO)
            .contentType(ContentType.JSON)
            .post(RFID_ENDPOINT).
        then()
            .statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Optional<RFIDLink> queryResult = rfidLinkRepository.findByRfid(rfidLinkDTO.get("rfid"));
        Assert.assertEquals(queryResult.orElseThrow(RFIDNotFoundException::new).
                getTicket().getId(), ticket.getId());
    }

    @Test
    public void testAddRFIDLinkInvalidRFIDAsAdmin() {
        User user = createUser(true);
        Ticket ticket = createTicketForUser(user);
        String invalid = "1234";

        HashMap<String, String> rfidLinkDTO = makeRFIDLinkDTO(ticket);
        rfidLinkDTO.put("rfid", invalid);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .body(rfidLinkDTO)
            .contentType(ContentType.JSON)
            .post(RFID_ENDPOINT).
        then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on

        Assert.assertFalse(rfidLinkRepository.findByRfid(invalid).isPresent());
    }

    @Test
    public void testAddRFIDLinkRFIDTakenAsAdmin() {
        User admin = createUser(true);
        User user = createUser();
        Ticket ticket1 = createTicketForUser(admin);
        Ticket ticket2 = createTicketForUser(user);
        RFIDLink rfidLink = createRfidLink(ticket1);
        HashMap<String, String> rfidLinkDTO = makeRFIDLinkDTO(ticket2);
        rfidLinkDTO.put("rfid", rfidLink.getRFID());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when()
            .body(rfidLinkDTO)
            .contentType(ContentType.JSON)
            .post(RFID_ENDPOINT).
        then()
            .statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on

        Optional<RFIDLink> queryResult = rfidLinkRepository.findByRfid(rfidLink.getRFID());
        Assert.assertTrue(queryResult.isPresent());
        Assert.assertEquals(queryResult.get().getTicket().getId(), ticket1.getId());
    }

    @Test
    public void testAddRFIDLinkTicketTakenAsAdmin() {
        User user = createUser(true);
        Ticket ticket = createTicketForUser(user);
        createRfidLink(ticket);
        HashMap<String, String> rfidLinkDTO = makeRFIDLinkDTO(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .body(rfidLinkDTO)
            .contentType(ContentType.JSON)
            .post(RFID_ENDPOINT).
        then()
            .statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on

        Assert.assertFalse(rfidLinkRepository.findByRfid(rfidLinkDTO.get("rfid")).isPresent());
    }

    @Test
    public void testAddRFIDLinkTicketInvalidAsAdmin() {
        User admin = createUser(true);
        Ticket ticket = createTicketForUser(admin);

        ticket.setValid(false);
        ticketRepository.saveAndFlush(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when()
            .body(makeRFIDLinkDTO(ticket))
            .contentType(ContentType.JSON)
            .post(RFID_ENDPOINT)
        .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testAddRFIDLinkTicketDoesntExistAsAdmin() {
        User user = createUser(true);
        Ticket ticket = createTicketForUser(user);

        HashMap<String, String> rfidLinkDTO = makeRFIDLinkDTO(ticket);
        rfidLinkDTO.put("ticketId", user.getId().toString());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .body(rfidLinkDTO)
            .contentType(ContentType.JSON)
            .post(RFID_ENDPOINT)
        .then()
            .statusCode(HttpStatus.SC_NOT_FOUND);
        //@formatter:on

        Assert.assertFalse(rfidLinkRepository.findByRfid(rfidLinkDTO.get("rfid")).isPresent());
    }

    @Test
    public void testRemoveRFIDLinkByRFIDAsAdmin() {
        User user = createUser(true);
        Ticket ticket = createTicketForUser(user);
        RFIDLink link = createRfidLink(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .delete(RFID_ENDPOINT + "/" + link.getRFID()).
        then()
            .statusCode(HttpStatus.SC_OK)
            .body("ticket.id", equalTo(ticket.getId().intValue()));
        //@formatter:on

        Assert.assertFalse(rfidLinkRepository.findByRfid(link.getRFID()).isPresent());
    }

    @Test
    public void testRemoveRFIDLinkByTicketIdAsAdmin() {
        User user = createUser(true);
        Ticket ticket = createTicketForUser(user);
        RFIDLink rfidLink = createRfidLink(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .delete(RFID_TICKET_ENDPOINT + "/" + ticket.getId()).
        then()
            .statusCode(HttpStatus.SC_OK)
            .body("ticket.id", equalTo(ticket.getId().intValue()));
        //@formatter:on

        Assert.assertFalse(rfidLinkRepository.findByRfid(rfidLink.getRFID()).isPresent());
    }

    @Test
    public void testRemoveRFIDLinkLinkNotFoundAsAdmin() {
        User user = createUser(true);
        Ticket ticket = createTicketForUser(user);
        RFIDLink link = createRfidLink(ticket);
        String unused = String.valueOf(Long.valueOf(link.getRFID()) + 1);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .delete(RFID_ENDPOINT + "/" + unused).
        then()
            .statusCode(HttpStatus.SC_NOT_FOUND);
        //@formatter:on

        Assert.assertTrue(rfidLinkRepository.findByRfid(link.getRFID()).isPresent());
    }

    @Test
    public void testRemoveRFIDLinkInvalidRFIDAsAdmin() {
        User user = createUser(true);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .delete(RFID_ENDPOINT + "/" + "123").
        then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    private HashMap<String, String> makeRFIDLinkDTO(Ticket ticket) {
        long rfid = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;

        HashMap<String, String> rfidLinkDTO = new HashMap<>();
        rfidLinkDTO.put("rfid", String.valueOf(rfid));
        rfidLinkDTO.put("ticketId", ticket.getId().toString());

        return rfidLinkDTO;
    }
}
