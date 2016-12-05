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
 *//*


package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.exception.TicketAlreadyLinkedException;
import ch.wisv.areafiftylan.exception.TokenNotFoundException;
import ch.wisv.areafiftylan.extras.rfid.model.RFIDLink;
import ch.wisv.areafiftylan.extras.rfid.model.RFIDLinkRepository;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.model.TicketType;
import ch.wisv.areafiftylan.products.service.repository.TicketRepository;
import ch.wisv.areafiftylan.security.token.TicketTransferToken;
import ch.wisv.areafiftylan.security.token.repository.TicketTransferTokenRepository;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.teams.service.TeamRepository;
import ch.wisv.areafiftylan.users.model.Gender;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.utils.SessionData;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

*/
/**
 * Created by beer on 5-1-16.
 *//*

public class TicketRestIntegrationTest extends IntegrationTest {
    private final String TICKETS_ENDPOINT = "/tickets";
    private final String TRANSFER_ENDPOINT = TICKETS_ENDPOINT + "/transfer";

    private User ticketReceiver;
    private final String ticketReceiverCleartextPassword = "password";
    private final String teamMemberCleartextPassword = "password";
    private Ticket ticket;

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private TicketTransferTokenRepository tttRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private RFIDLinkRepository rfidLinkRepository;

    @Before
    public void initTransferTest() {
        ticketReceiver = makeTicketReceiver();
        ticket = makeTicket();
    }

    @After
    public void cleanTransferTest() {
        rfidLinkRepository.deleteAll();
        teamRepository.deleteAll();
        tttRepository.deleteAll();
        ticketRepository.deleteAll();
    }

    private User makeTicketReceiver() {
        User receiver = new User("receiver@mail.com", new BCryptPasswordEncoder().encode(ticketReceiverCleartextPassword));
        receiver.getProfile()
                .setAllFields("receiver", " of tickets", "GotYaTicket", localDate, Gender.MALE, "Money Owner 4", "2826GJ",
                        "Tomorrowland", "0906-1111", null);

        return userRepository.saveAndFlush(receiver);
    }

    private Ticket makeTicket() {
        Ticket t = new Ticket(user, TicketType.EARLY_FULL, false, false);
        t.setValid(true);
        return ticketRepository.saveAndFlush(t);
    }

    @Test
    public void testGetAllTickets_Anon() {
        //@formatter:off
        when()
            .get("/tickets")
        .then()
            .statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetAllTickets_User() {
        SessionData login = login(user.getUsername(), userCleartextPassword);


        //@formatter:off
        given()
                .filter(sessionFilter)
                .header(login.getCsrfHeader())
        .when()
                .get("/tickets")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetAllTickets_Admin() {
        SessionData login = login(admin.getUsername(), adminCleartextPassword);

        //@formatter:off
        given()
                .filter(sessionFilter)
                .header(login.getCsrfHeader())
        .when()
                .get("/tickets")
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$", hasSize(1));
        //@formatter:on
    }

    @Test
    public void testAddTransfer_Anon() {
        given().
                when().
                content(ticketReceiver.getUsername()).contentType(ContentType.TEXT).
                post(TRANSFER_ENDPOINT + "/" + ticket.getId()).
                then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        Assert.assertTrue(tttRepository.count() == 0);
    }

    @Test
    public void testAddTransfer_Owner() {
        String token = addTicketTransfer(user.getUsername(), userCleartextPassword).then().statusCode(HttpStatus.SC_OK)
                .extract().path("object");

        TicketTransferToken ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ttt.getTicket().getOwner().equals(user));
        Assert.assertTrue(ttt.getUser().equals(ticketReceiver));
    }

    @Test
    public void testAddTransfer_OwnerDifferentCase() {
        String token = addTicketTransfer(user.getUsername().toUpperCase(), userCleartextPassword).then()
                .statusCode(HttpStatus.SC_OK).extract().path("object");

        TicketTransferToken ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ttt.getTicket().getOwner().equals(user));
        Assert.assertTrue(ttt.getUser().equals(ticketReceiver));
    }

    @Test
    public void testAddTransfer_Receiver() {
        addTicketTransfer(ticketReceiver.getUsername(), ticketReceiverCleartextPassword).then()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        Assert.assertTrue(tttRepository.count() == 0);
    }

    @Test
    public void testAddTransfer_Outsider() {
        addTicketTransfer(outsider.getUsername(), outsiderCleartextPassword).then().statusCode(HttpStatus.SC_FORBIDDEN);

        Assert.assertTrue(tttRepository.count() == 0);
    }

    @Test
    public void testDoTransfer_Anon() {
        TicketTransferToken ttt = addTicketTransferGetToken();

        given().
                when().
                content(ttt.getToken()).
                put(TRANSFER_ENDPOINT).
                then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        String token = ttt.getToken();
        ticket = ticketRepository.findOne(ticket.getId());
        ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(user));
    }

    @Test
    public void testDoTransfer_Owner() {
        TicketTransferToken ttt = addTicketTransferGetToken();

        SessionData login = login(user.getUsername(), userCleartextPassword);

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
                when().
                content(ttt.getToken()).
                put(TRANSFER_ENDPOINT).
                then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        String token = ttt.getToken();
        ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(user));
    }

    @Test
    public void testDoTransfer_Receiver() {
        TicketTransferToken ttt = addTicketTransferGetToken();

        SessionData login = login(ticketReceiver.getUsername(), ticketReceiverCleartextPassword);

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
                when().
                content(ttt.getToken()).
                put(TRANSFER_ENDPOINT).
                then().
                statusCode(HttpStatus.SC_OK);

        String token = ttt.getToken();
        ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(!ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(ticketReceiver));
    }

    @Test
    public void testDoTransferTicketLinked_Receiver() {
        TicketTransferToken ttt = addTicketTransferGetToken();
        TicketAlreadyLinkedException e = new TicketAlreadyLinkedException();

        SessionData login = login(ticketReceiver.getUsername(), ticketReceiverCleartextPassword);

        rfidLinkRepository.saveAndFlush(new RFIDLink("1212121212", ticket));

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
        when().
                content(ttt.getToken()).
                put(TRANSFER_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_CONFLICT).
                body(containsString(e.getMessage()));

        String token = ttt.getToken();
        ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(user));
    }

    @Test
    public void testDoTransfer_Outsider() {
        TicketTransferToken ttt = addTicketTransferGetToken();

        SessionData login = login(outsider.getUsername(), outsiderCleartextPassword);

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
                when().
                content(ttt.getToken()).
                put(TRANSFER_ENDPOINT).
                then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        String token = ttt.getToken();
        ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(user));
    }

    @Test
    public void testDuplicateSetupForTransfer() {
        addTicketTransfer(user.getUsername(), userCleartextPassword);
        Response response = addTicketTransfer(user.getUsername(), userCleartextPassword);

        //@formatter:off
        response.then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testDuplicateSetupForTransferDifferentCase() {
        addTicketTransfer(user.getUsername(), userCleartextPassword);
        Response response = addTicketTransfer(user.getUsername().toUpperCase(), userCleartextPassword);

        //@formatter:off
        response.then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testRFIDLinkedSetupForTransfer() {
        TicketAlreadyLinkedException e = new TicketAlreadyLinkedException();

        rfidLinkRepository.save(new RFIDLink("1212121212", ticket));

        addTicketTransfer(user.getUsername(), userCleartextPassword)
                .then()
                .statusCode(HttpStatus.SC_CONFLICT)
                .body(containsString(e.getMessage()));
    }

    @Test
    public void testCancelTransfer_Anon() {
        TicketTransferToken ttt = addTicketTransferGetToken();

        given().
                when().
                content(ttt.getToken()).
                delete(TRANSFER_ENDPOINT).
                then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        String token = ttt.getToken();
        ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(user));
    }

    @Test
    public void testCancelTransfer_Owner() {
        TicketTransferToken ttt = addTicketTransferGetToken();

        SessionData login = login(user.getUsername(), userCleartextPassword);

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
                when().
                content(ttt.getToken()).
                delete(TRANSFER_ENDPOINT).
                then().
                statusCode(HttpStatus.SC_OK);

        String token = ttt.getToken();
        ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(!ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(user));
    }

    @Test
    public void testCancelTransfer_Receiver() {
        TicketTransferToken ttt = addTicketTransferGetToken();

        SessionData login = login(ticketReceiver.getUsername(), ticketReceiverCleartextPassword);

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
                when().
                content(ttt.getToken()).
                delete(TRANSFER_ENDPOINT).
                then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        String token = ttt.getToken();
        ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(user));
    }

    @Test
    public void testCancelTransfer_OutsideUser() {
        TicketTransferToken ttt = addTicketTransferGetToken();

        SessionData login = login(outsider.getUsername(), outsiderCleartextPassword);

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
                when().
                content(ttt.getToken()).
                delete(TRANSFER_ENDPOINT).
                then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        String token = ttt.getToken();
        ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(user));
    }

    private Response addTicketTransfer(String uname, String pw) {
        SessionData login = login(uname, pw);

        Response r = given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
                when().
                content(ticketReceiver.getUsername()).contentType(ContentType.TEXT).
                post(TRANSFER_ENDPOINT + "/" + ticket.getId()).
                then().
                extract().response();

        logout();

        return r;
    }

    private TicketTransferToken addTicketTransferGetToken() {
        String token = addTicketTransfer(user.getUsername(), userCleartextPassword).
                then().
                extract().path("object");

        return tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
    }

    @Test
    public void testGetTicketInControlNoTickets() {
        SessionData login = login(user.getUsername(), userCleartextPassword);

        ticketRepository.delete(ticket);

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
                when().
                get(TICKETS_ENDPOINT + "/teammembers").
                then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(0));
    }

    @Test
    public void testGetTicketInControlOwnTicket() {
        SessionData login = login(user.getUsername(), userCleartextPassword);

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
                when().
                get(TICKETS_ENDPOINT + "/teammembers").
                then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(1)).
                body("owner.username", containsInAnyOrder(user.getUsername()));
    }

    @Test
    public void testGetTicketInControlTeamMemberTicket() {
        User teamMate = createTeamReturnSingleMember();

        SessionData login = login(user.getUsername(), userCleartextPassword);

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
                when().
                get(TICKETS_ENDPOINT + "/teammembers").
                then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(2)).
                body("owner.username", containsInAnyOrder(user.getUsername(), teamMate.getUsername()));
    }

    @Test
    public void testGetTicketInControlMemberOfTeam() {
        User teamMate = createTeamReturnSingleMember();

        SessionData login = login(teamMate.getUsername(), teamMemberCleartextPassword);

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
                when().
                get(TICKETS_ENDPOINT + "/teammembers").
                then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(1)).
                body("owner.username", containsInAnyOrder(teamMate.getUsername()));
    }

    private User createTeamReturnSingleMember() {
        User teamMate = new User("teammate@email.com", new BCryptPasswordEncoder().encode(teamMemberCleartextPassword));
        teamMate.getProfile()
                .setAllFields("Team", "Mate", "IloveYOU", localDate, Gender.MALE, "Buddy 7", "2826GJ", "Holland", "0906-7777",
                        null);
        userRepository.saveAndFlush(teamMate);

        Ticket teamTicket = new Ticket(teamMate, TicketType.REGULAR_FULL, false, false);
        teamTicket.setValid(true);
        ticketRepository.saveAndFlush(teamTicket);

        Team team = new Team("Superawesome", user);
        team.addMember(teamMate);
        teamRepository.saveAndFlush(team);

        return teamMate;
    }

    @Test
    public void testGetAllTicketsForTransport_Anon() {
        //@formatter:off
        when()
            .get("/tickets/transport")
        .then()
            .statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetAllTicketsForTransport_User() {
        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given()
            .filter(sessionFilter)
            .header(login.getCsrfHeader())
        .when()
            .get("/tickets/transport")
        .then()
            .statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testGetAllTicketsForTransport_Admin() {
        makeTicket();
        ticketRepository.saveAndFlush(new Ticket(user, TicketType.REGULAR_FULL, true, false));

        SessionData login = login(admin.getUsername(), adminCleartextPassword);

        //@formatter:off
        given()
            .filter(sessionFilter)
            .header(login.getCsrfHeader())
        .when()
            .get("/tickets/transport")
        .then()
            .statusCode(HttpStatus.SC_OK)
            .body("$", hasSize(1))
            .body("pickupService", contains(true));
        //@formatter:on
    }
}
*/
