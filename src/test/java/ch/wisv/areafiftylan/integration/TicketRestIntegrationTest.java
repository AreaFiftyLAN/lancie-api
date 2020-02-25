/*
 * Copyright (c) 2018  W.I.S.V. 'Christiaan Huygens'
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


package ch.wisv.areafiftylan.integration;

import ch.wisv.areafiftylan.exception.TicketAlreadyLinkedException;
import ch.wisv.areafiftylan.exception.TicketNotFoundException;
import ch.wisv.areafiftylan.extras.rfid.model.RFIDLink;
import ch.wisv.areafiftylan.extras.rfid.service.RFIDLinkRepository;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.model.TicketOption;
import ch.wisv.areafiftylan.products.model.TicketType;
import ch.wisv.areafiftylan.products.service.TicketService;
import ch.wisv.areafiftylan.products.service.repository.TicketRepository;
import ch.wisv.areafiftylan.security.token.TicketTransferToken;
import ch.wisv.areafiftylan.security.token.repository.TicketTransferTokenRepository;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.users.model.User;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;


public class TicketRestIntegrationTest extends XAuthIntegrationTest {
    private final String TICKETS_ENDPOINT = "/tickets";
    private final String TRANSFER_ENDPOINT = TICKETS_ENDPOINT + "/transfer";
    private final String TRANSPORT_ENDPOINT = TICKETS_ENDPOINT + "/transport/" + PICKUP_SERVICE;

    @Value("${a5l.ticketLimit}")
    private int TICKET_LIMIT;

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private TicketTransferTokenRepository tttRepository;
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
        User admin = createAdmin();

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
    public void testGetAvailableTickets() {
        Collection<TicketType> ticketTypes =
                ticketService.getAllTicketTypes().stream().filter(TicketType::isBuyable).collect(Collectors.toList());

        //@formatter:off
        given().
        when().
            get(TICKETS_ENDPOINT + "/available").
        then().
            statusCode(HttpStatus.SC_OK).
            body("ticketTypes.ticketType", is(ticketTypes.stream()
                    .map(TicketType::getName)
                    .collect(Collectors.toList()))).
            body("ticketTypes.price", is(ticketTypes.stream()
                    .map(TicketType::getPrice)
                    .collect(Collectors.toList()))).
            body("ticketLimit", is(TICKET_LIMIT));
        //@formatter:on
    }

    @Test
    public void testGetTicketTypesAsAnon() {
        //@formatter:off
        given().
        when().
            get(TICKETS_ENDPOINT + "/types").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetTicketTypesAsAdmin() {
        User admin = createAdmin();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            get(TICKETS_ENDPOINT + "/types").
        then().
            statusCode(HttpStatus.SC_OK).
            body("name", hasSize(ticketService.getAllTicketTypes().size()));
        //@formatter:on
    }

    @Test
    public void testAddTicketType() {
        User admin = createAdmin();
        TicketType type =
                new TicketType("testAddType", "Type for adding test", 10, 0, LocalDateTime.now().plusDays(1), false);

        //@formatter:off
            given().
                header(getXAuthTokenHeaderForUser(admin)).
            when().
                contentType(ContentType.JSON).
                body(type).
                post(TICKETS_ENDPOINT + "/types").
            then().
                statusCode(HttpStatus.SC_CREATED);
        //@formatter:on
    }

    @Test
    public void testRemoveTicketType() {
        User admin = createAdmin();
        TicketType type =
                new TicketType("testRemoveType", "Type for remove test", 10, 0, LocalDateTime.now().plusDays(1), false);
        type = ticketService.addTicketType(type);
        //@formatter:off
            given().
                header(getXAuthTokenHeaderForUser(admin)).
            when().
                delete(TICKETS_ENDPOINT + "/types/" + type.getId()).
            then().
                statusCode(HttpStatus.SC_OK);
        //@formatter:on

        assertThat(ticketService.getAllTicketTypes()).doesNotContain(type);
    }

    @Test
    public void testEditTicketType() {
        User admin = createAdmin();
        TicketType type = new TicketType("testEditType", "Type for edit test", 10, 0, LocalDateTime.now(), false);
        type = ticketService.addTicketType(type);

        type.setPrice(15F);
        //@formatter:off
            given().
                header(getXAuthTokenHeaderForUser(admin)).
            when().
                contentType(ContentType.JSON).
                body(type).
                put(TICKETS_ENDPOINT + "/types/" + type.getId()).
            then().
                statusCode(HttpStatus.SC_OK).
                body("object.price", is(15F));
        //@formatter:on

        assertThat(ticketService.getAllTicketTypes()).contains(type);
    }

    @Test
    public void testAddTicketOption() {
        TicketOption option = new TicketOption("addTest", 10);

        User admin = createAdmin();
        //@formatter:off
            given().
                header(getXAuthTokenHeaderForUser(admin)).
            when().
                contentType(ContentType.JSON).
                body(option).
                post(TICKETS_ENDPOINT + "/options").
            then().
                statusCode(HttpStatus.SC_CREATED);
        //@formatter:on
    }

    @Test
    public void testAddTransferAsAnon() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);

        //@formatter:off
        given().
            body(ticketReceiver.getEmail()).
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
                body(ticketReceiver.getEmail()).
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
            body(ticketReceiver.getEmail()).
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
            body(ticketOwner.getEmail()).
        when().
            post(TRANSFER_ENDPOINT + "/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
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
            body(ticketReceiver.getEmail()).
        when().
            post(TRANSFER_ENDPOINT + "/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetValidTokens() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);

        TicketTransferToken transferToken = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getEmail());

        //@formatter:off
            given().
                header(getXAuthTokenHeaderForUser(ticketOwner)).
            when().
                get(TICKETS_ENDPOINT + "/tokens").
            then().
                statusCode(HttpStatus.SC_OK).
                body("object.token", contains(transferToken.getToken()));
        //@formatter:on
    }

    @Test
    public void testGetValidTokensMultipleTickets() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        Ticket ticket2 = createTicketForUser(ticketOwner);

        TicketTransferToken transferToken = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getEmail());
        TicketTransferToken transferToken2 = ticketService.setupForTransfer(ticket2.getId(), ticketReceiver.getEmail());

        //@formatter:off
            given().
                header(getXAuthTokenHeaderForUser(ticketOwner)).
            when().
                get(TICKETS_ENDPOINT + "/tokens").
            then().
                statusCode(HttpStatus.SC_OK).
                body("object.token", contains(transferToken.getToken(), transferToken2.getToken()));
        //@formatter:on
    }

    @Test
    public void testDoTransferAsAnon() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        TicketTransferToken ttt = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getEmail());

        //@formatter:off
        given().
            body(ttt.getToken()).
        when().
            put(TRANSFER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on

        ttt = tttRepository.findByToken(ttt.getToken()).get();
        ticket = ticketRepository.findById(ticket.getId()).orElseThrow(TicketNotFoundException::new);

        Assert.assertTrue(ttt.isValid());
        Assert.assertEquals(ticket.getOwner(), ticketOwner);
    }

    @Test
    public void testDoTransferAsOwner() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        TicketTransferToken ttt = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getEmail());

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
        ticket = ticketRepository.findById(ticket.getId()).orElseThrow(TicketNotFoundException::new);

        Assert.assertTrue(ttt.isValid());
        Assert.assertEquals(ticket.getOwner(), ticketOwner);
    }

    @Test
    public void testDoTransferAsReceiver() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        TicketTransferToken ttt = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getEmail());

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
        ticket = ticketRepository.findById(ticket.getId()).orElseThrow(TicketNotFoundException::new);

        Assert.assertFalse(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(ticketReceiver));
    }

    @Test
    public void testDoTransferTicketLinkedAsReceiver() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);

        TicketTransferToken ttt = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getEmail());

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
        ticket = ticketRepository.findById(ticket.getId()).orElseThrow(TicketNotFoundException::new);

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(ticketOwner));
    }

    @Test
    public void testDoTransferAsOutsider() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        User outsider = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        TicketTransferToken ttt = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getEmail());

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
        ticket = ticketRepository.findById(ticket.getId()).orElseThrow(TicketNotFoundException::new);

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(ticketOwner));
    }

    @Test
    public void testDuplicateSetupForTransfer() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getEmail());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(ticketOwner)).
            body(ticketReceiver.getEmail()).
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
            body(ticketReceiver.getEmail()).
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
        TicketTransferToken ttt = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getEmail());

        //@formatter:off
        given().
            body(ttt.getToken()).
        when().
            delete(TRANSFER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on

        ttt = tttRepository.findByToken(ttt.getToken()).get();
        ticket = ticketRepository.findById(ticket.getId()).orElseThrow(TicketNotFoundException::new);

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(ticketOwner));
    }

    @Test
    public void testCancelTransferAsOwner() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        TicketTransferToken ttt = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getEmail());

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
        ticket = ticketRepository.findById(ticket.getId()).orElseThrow(TicketNotFoundException::new);

        Assert.assertFalse(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(ticketOwner));
    }

    @Test
    public void testCancelTransferAsReceiver() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        TicketTransferToken ttt = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getEmail());

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
        ticket = ticketRepository.findById(ticket.getId()).orElseThrow(TicketNotFoundException::new);

        Assert.assertTrue(ttt.isValid());
        Assert.assertEquals(ticket.getOwner(), ticketOwner);
    }

    @Test
    public void testCancelTransferAsOutsider() {
        User ticketOwner = createUser();
        User ticketReceiver = createUser();
        User outsider = createUser();
        Ticket ticket = createTicketForUser(ticketOwner);
        TicketTransferToken ttt = ticketService.setupForTransfer(ticket.getId(), ticketReceiver.getEmail());

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
        ticket = ticketRepository.findById(ticket.getId()).orElseThrow(TicketNotFoundException::new);

        Assert.assertTrue(ttt.isValid());
        Assert.assertEquals(ticket.getOwner(), ticketOwner);
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
            body("owner.email", containsInAnyOrder(user.getEmail()));
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
            body("owner.email", containsInAnyOrder(user.getEmail(), teammate.getEmail()));
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
            body("owner.email", containsInAnyOrder(teammate.getEmail()));
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

        User admin = createAdmin();
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


    @Test
    public void testExportAsAnon() {
        //@formatter:off
        given().
        when().
            get("/export").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetExportAsAdmin() {
        User admin = createAdmin();
        createTicket(admin, Collections.singletonList(PICKUP_SERVICE));

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            get("/export").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

    }

    @Test
    public void testChangeBuyable() {
        User admin = createAdmin();
        TicketType ticket = getTicketType();
        ticket.setBuyable(false);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            body(ticket).
            contentType(ContentType.JSON).
            put(TICKETS_ENDPOINT + "/tickettype/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.buyable", equalTo(false));
        //@formatter:on
    }

    @Test
    public void testAddOptionToType() {
        User admin = createAdmin();
        TicketType ticket = getTicketType();
        TicketOption option = new TicketOption("testOption", 2);
        ticketService.addTicketOption(option);
        //@formatter:off
        given().
                header(getXAuthTokenHeaderForUser(admin)).
        when().
            body(option).
            contentType(ContentType.JSON).
            post(TICKETS_ENDPOINT + "/types/" + ticket.getId() + "/option").
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.possibleOptions.id", hasItem(Math.toIntExact(option.getId())));
        //@formatter:on
    }
}
