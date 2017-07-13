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
import ch.wisv.areafiftylan.extras.rfid.model.RFIDLinkDTO;
import ch.wisv.areafiftylan.extras.rfid.model.RFIDLinkRepository;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.service.repository.TicketRepository;
import ch.wisv.areafiftylan.users.model.User;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

public class RFIDIntegrationTest extends XAuthIntegrationTest {
    private final String RFID_ENDPOINT = "/rfid/";
    private final String RFID_TICKET_ENDPOINT = RFID_ENDPOINT + "tickets/";
    private final String INVALID_RFID = "123";

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

    private RFIDLinkDTO makeRFIDLinkDTO(Ticket ticket) {
        long rfid = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        RFIDLinkDTO dto = new RFIDLinkDTO();
        dto.setRfid(String.valueOf(rfid));
        dto.setTicketId(ticket.getId());
        return dto;
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
        User user = createAdmin();

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
        User admin = createAdmin();
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
        User admin = createAdmin();
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
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);
        RFIDLink link = createRfidLink(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .get(RFID_ENDPOINT + link.getRfid() + "/ticketId").
        then()
            .statusCode(HttpStatus.SC_OK)
            .body(equalTo(ticket.getId().toString()));
        //@formatter:on
    }

    @Test
    public void testGetTicketIdInvalidRFIDAsAdmin() {
        User user = createAdmin();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .get(RFID_ENDPOINT + INVALID_RFID + "/ticketId").
        then()
            .statusCode(HttpStatus.SC_NOT_FOUND);
        //@formatter:on
    }

    @Test
    public void testGetTicketIdUnusedRFIDAsAdmin() {
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);
        RFIDLink link = createRfidLink(ticket);
        String unused = String.valueOf(Long.valueOf(link.getRfid()) + 1);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .get(RFID_ENDPOINT + unused + "/ticketId").
        then()
            .statusCode(HttpStatus.SC_NOT_FOUND);
        //@formatter:on
    }

    @Test
    public void testGetUserByRFID(){
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);
        RFIDLink link = createRfidLink(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .get(RFID_ENDPOINT + link.getRfid() + "/user").
        then()
            .statusCode(HttpStatus.SC_OK)
            .body("email", equalTo(user.getEmail()))
            .body("profile.displayName", equalTo(user.getProfile().getDisplayName()))
            .body("id", equalTo(user.getId().intValue()));
        //@formatter:on
    }

    @Test
    public void testGetUserByInvalidRFID(){
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);
        RFIDLink link = createRfidLink(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .get(RFID_ENDPOINT + INVALID_RFID + "/user").
        then()
            .statusCode(HttpStatus.SC_NOT_FOUND);
        //@formatter:on
    }

    @Test
    public void testAddRFIDLinkAsAdmin() {
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);
        RFIDLinkDTO dto = makeRFIDLinkDTO(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .body(dto)
            .contentType(ContentType.JSON)
            .post(RFID_ENDPOINT).
        then()
            .statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Optional<RFIDLink> queryResult = rfidLinkRepository.findByRfid(dto.getRfid());
        Assert.assertEquals(queryResult.orElseThrow(RFIDNotFoundException::new).
                getTicket().getId(), ticket.getId());
    }

    @Test
    public void testAddRFIDLinkInvalidRFIDAsAdmin() {
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);

        RFIDLinkDTO dto = makeRFIDLinkDTO(ticket);
        dto.setRfid(INVALID_RFID);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .body(dto)
            .contentType(ContentType.JSON)
            .post(RFID_ENDPOINT).
        then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on

        Assert.assertFalse(rfidLinkRepository.findByRfid(INVALID_RFID).isPresent());
    }

    @Test
    public void testAddRFIDLinkRFIDTakenAsAdmin() {
        User admin = createAdmin();
        User user = createUser();
        Ticket ticket1 = createTicketForUser(admin);
        Ticket ticket2 = createTicketForUser(user);
        RFIDLink rfidLink = createRfidLink(ticket1);
        RFIDLinkDTO dto = makeRFIDLinkDTO(ticket2);
        dto.setRfid(rfidLink.getRfid());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when()
            .body(dto)
            .contentType(ContentType.JSON)
            .post(RFID_ENDPOINT).
        then()
            .statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on

        Optional<RFIDLink> queryResult = rfidLinkRepository.findByRfid(rfidLink.getRfid());
        Assert.assertTrue(queryResult.isPresent());
        Assert.assertEquals(queryResult.get().getTicket().getId(), ticket1.getId());
    }

    @Test
    public void testAddRFIDLinkTicketTakenAsAdmin() {
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);
        createRfidLink(ticket);
        RFIDLinkDTO dto = makeRFIDLinkDTO(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .body(dto)
            .contentType(ContentType.JSON)
            .post(RFID_ENDPOINT).
        then()
            .statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on

        Assert.assertFalse(rfidLinkRepository.findByRfid(dto.getRfid()).isPresent());
    }

    @Test
    public void testAddRFIDLinkTicketInvalidAsAdmin() {
        User admin = createAdmin();
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
        User admin = createAdmin();
        Ticket ticket = createTicketForUser(admin);

        RFIDLinkDTO dto = makeRFIDLinkDTO(ticket);
        dto.setTicketId(admin.getId());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when()
            .body(dto)
            .contentType(ContentType.JSON)
            .post(RFID_ENDPOINT)
        .then()
            .statusCode(HttpStatus.SC_NOT_FOUND);
        //@formatter:on

        Assert.assertFalse(rfidLinkRepository.findByRfid(dto.getRfid()).isPresent());
    }

    @Test
    public void testRemoveRFIDLinkByRFIDAsAdmin() {
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);
        RFIDLink link = createRfidLink(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .delete(RFID_ENDPOINT + link.getRfid()).
        then()
            .statusCode(HttpStatus.SC_OK)
            .body("ticket.id", equalTo(ticket.getId().intValue()));
        //@formatter:on

        Assert.assertFalse(rfidLinkRepository.findByRfid(link.getRfid()).isPresent());
    }

    @Test
    public void testRemoveRFIDLinkByTicketIdAsAdmin() {
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);
        RFIDLink rfidLink = createRfidLink(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .delete(RFID_TICKET_ENDPOINT + ticket.getId()).
        then()
            .statusCode(HttpStatus.SC_OK)
            .body("ticket.id", equalTo(ticket.getId().intValue()));
        //@formatter:on

        Assert.assertFalse(rfidLinkRepository.findByRfid(rfidLink.getRfid()).isPresent());
    }

    @Test
    public void testRemoveRFIDLinkLinkNotFoundAsAdmin() {
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);
        RFIDLink link = createRfidLink(ticket);
        String unused = String.valueOf(Long.valueOf(link.getRfid()) + 1);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .delete(RFID_ENDPOINT + unused).
        then()
            .statusCode(HttpStatus.SC_NOT_FOUND);
        //@formatter:on

        Assert.assertTrue(rfidLinkRepository.findByRfid(link.getRfid()).isPresent());
    }

    @Test
    public void testRemoveRFIDLinkInvalidRFIDAsAdmin() {
        User user = createAdmin();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when()
            .delete(RFID_ENDPOINT + INVALID_RFID).
        then()
            .statusCode(HttpStatus.SC_NOT_FOUND);
        //@formatter:on
    }
}
