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

import ch.wisv.areafiftylan.exception.TicketAlreadyLinkedException;
import ch.wisv.areafiftylan.extras.rfid.model.RFIDLink;
import ch.wisv.areafiftylan.extras.rfid.model.RFIDLinkRepository;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.service.TicketService;
import ch.wisv.areafiftylan.products.service.repository.TicketRepository;
import ch.wisv.areafiftylan.security.token.TicketTransferToken;
import ch.wisv.areafiftylan.security.token.repository.TicketTransferTokenRepository;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.teams.service.TeamRepository;
import ch.wisv.areafiftylan.users.model.User;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;


public class TicketRestIntegrationTest extends XAuthIntegrationTest {
    private final String TICKETS_ENDPOINT = "/tickets";
    private final String TRANSFER_ENDPOINT = TICKETS_ENDPOINT + "/transfer";
    private final String TRANSPORT_ENDPOINT = TICKETS_ENDPOINT + "/transport";

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private TicketTransferTokenRepository tttRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private RFIDLinkRepository rfidLinkRepository;
    @Autowired
    private TicketService ticketService;

    @Test
    public void testGetAllTicketsAsAnon() {
        //@formatter:off
        when().
            get(TICKETS_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetAllTicketsAsUser() {
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(TICKETS_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetAllTicketsAsAdmin() {
        User admin = createUser(true);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            get(TICKETS_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testAddTransferAsAnon() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);

        //@formatter:off
        given().
            body(ticketReceiver.getUsername()).
        when().
            post(TRANSFER_ENDPOINT + "/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAddTransferAsOwner() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);

        //@formatter:off
        String token =
            given().
                header(getXAuthTokenHeaderForUser(ticketOwner)).
                body(ticketReceiver.getUsername()).
            when().
                post(TRANSFER_ENDPOINT + "/" + ticket.getId()).
            then().
                statusCode(HttpStatus.SC_OK).
                extract().path("object");
        //@formatter:on

        TicketTransferToken ttt = tttRepository.findByToken(token).get();
        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ttt.getTicket().getOwner().equals(ticketOwner));
        Assert.assertTrue(ttt.getUser().equals(ticketReceiver));
    }

    @Test
    public void testAddTransferAsReceiver() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(ticketReceiver)).
            body(ticketReceiver.getUsername()).
        when().
            post(TRANSFER_ENDPOINT + "/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAddTransferToSelf() {
        User ticketOwner = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(ticketOwner)).
            body(ticketOwner.getUsername()).
        when().
            post(TRANSFER_ENDPOINT + "/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_NOT_MODIFIED);
        //@formatter:on
    }

    @Test
    public void testAddTransferAsOutsider() {
        User outsider = createUser();
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(outsider)).
            body(ticketReceiver.getUsername()).
        when().
            post(TRANSFER_ENDPOINT + "/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testDoTransferAsAnon() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        TicketTransferToken ttt = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getUsername());

        //@formatter:off
        given().
            body(ttt.getToken()).
        when().
            put(TRANSFER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on

        ttt = tttRepository.findByToken(ttt.getToken()).get();
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(ticketOwner));
    }

    @Test
    public void testDoTransferAsOwner() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        TicketTransferToken ttt = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getUsername());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(ticketOwner)).
            body(ttt.getToken()).
        when().
            put(TRANSFER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on

        ttt = tttRepository.findByToken(ttt.getToken()).get();
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(ticketOwner));
    }

    @Test
    public void testDoTransferAsReceiver() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        TicketTransferToken ttt = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getUsername());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(ticketReceiver)).
            body(ttt.getToken()).
        when().
            put(TRANSFER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        ttt = tttRepository.findByToken(ttt.getToken()).get();
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertFalse(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(ticketReceiver));
    }

    @Test
    public void testDoTransferTicketLinkedAsReceiver() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);

        TicketTransferToken ttt = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getUsername());

        TicketAlreadyLinkedException e = new TicketAlreadyLinkedException();

        rfidLinkRepository.saveAndFlush(new RFIDLink("1212121212", ticket));

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(ticketReceiver)).
            body(ttt.getToken()).
        when().
            put(TRANSFER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_CONFLICT).
            body(containsString(e.getMessage()));
        //@formatter:on

        ttt = tttRepository.findByToken(ttt.getToken()).get();
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(ticketOwner));
    }

    @Test
    public void testDoTransferAsOutsider() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        User outsider = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        TicketTransferToken ttt = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getUsername());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(outsider)).
            body(ttt.getToken()).
        when().
            put(TRANSFER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on

        ttt = tttRepository.findByToken(ttt.getToken()).get();
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(ticketOwner));
    }

    @Test
    public void testDuplicateSetupForTransfer() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getUsername());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(ticketOwner)).
            body(ticketReceiver.getUsername()).
        when().
            post(TRANSFER_ENDPOINT + "/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testRFIDLinkedSetupForTransfer() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        rfidLinkRepository.save(new RFIDLink("1212121212", ticket));

        TicketAlreadyLinkedException e = new TicketAlreadyLinkedException();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(ticketOwner)).
            body(ticketReceiver.getUsername()).
        when().
            post(TRANSFER_ENDPOINT + "/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_CONFLICT).
            body(containsString(e.getMessage()));
        //@formatter:on
    }

    @Test
    public void testCancelTransferAsAnon() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        TicketTransferToken ttt = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getUsername());

        //@formatter:off
        given().
            body(ttt.getToken()).
        when().
            delete(TRANSFER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on

        ttt = tttRepository.findByToken(ttt.getToken()).get();
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(ticketOwner));
    }

    @Test
    public void testCancelTransferAsOwner() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        TicketTransferToken ttt = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getUsername());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(ticketOwner)).
            body(ttt.getToken()).
        when().
            delete(TRANSFER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        ttt = tttRepository.findByToken(ttt.getToken()).get();
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertFalse(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(ticketOwner));
    }

    @Test
    public void testCancelTransferAsReceiver() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        TicketTransferToken ttt = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getUsername());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(ticketReceiver)).
            body(ttt.getToken()).
        when().
            delete(TRANSFER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on

        ttt = tttRepository.findByToken(ttt.getToken()).get();
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(ticketOwner));
    }

    @Test
    public void testCancelTransferAsOutsider() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        User outsider = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        TicketTransferToken ttt = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getUsername());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(outsider)).
            body(ttt.getToken()).
        when().
            delete(TRANSFER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on

        ttt = tttRepository.findByToken(ttt.getToken()).get();
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(ticketOwner));
    }

    @Test
    public void testGetTicketInControlNoTickets() {
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(TICKETS_ENDPOINT + "/teammembers").
        then().
            statusCode(HttpStatus.SC_OK).
            body("$", hasSize(0));
        //@formatter:on
    }

    @Test
    public void testGetTicketInControlOwnTicket() {
        User user = createUser();
        createTicketForUser(user);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(TICKETS_ENDPOINT + "/teammembers").
        then().
            statusCode(HttpStatus.SC_OK).
            body("$", hasSize(1)).
            body("owner.username", containsInAnyOrder(user.getUsername()));
        //@formatter:on
    }

    @Test
    public void testGetTicketInControlTeamMemberTicket() {
        User user = createUser();
        createTicketForUser(user);
        User teammate = createUser();
        createTicketForUser(teammate);

        Team team = createTeamWithCaptain(user);
        addMemberToTeam(team, teammate);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(TICKETS_ENDPOINT + "/teammembers").
        then().
            statusCode(HttpStatus.SC_OK).
            body("owner.username", containsInAnyOrder(user.getUsername(), teammate.getUsername()));
        //@formatter:on
    }

    @Test
    public void testGetTicketInControlMemberOfTeam() {
        User user = createUser();
        User teammate = createUser();
        createTicketForUser(teammate);

        Team team = createTeamWithCaptain(user);
        addMemberToTeam(team, teammate);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(TICKETS_ENDPOINT + "/teammembers").
        then().
            statusCode(HttpStatus.SC_OK).
            body("owner.username", containsInAnyOrder(teammate.getUsername()));
        //@formatter:on
    }

    @Test
    public void testGetAllTicketsForTransportAsAnon() {
        //@formatter:off
        when().
            get(TRANSPORT_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetAllTicketsForTransportAsUser() {
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(TRANSPORT_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetAllTicketsForTransportAsAdmin() {
        // Very hard to check if all tickets have pickupservice, but
        // this is tested in the unit test as well

        User admin = createUser(true);
        createTicket(admin, Collections.singletonList(PICKUP_SERVICE));

        //@formatter:off
        Object enabledOptions = given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            get(TRANSPORT_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }
}
