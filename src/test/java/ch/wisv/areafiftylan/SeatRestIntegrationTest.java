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

import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Gender;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.service.repository.SeatRepository;
import ch.wisv.areafiftylan.service.repository.TeamRepository;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import ch.wisv.areafiftylan.util.SessionData;
import com.jayway.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

/**
 * Created by Sille Kamoen on 17-11-15.
 */
public class SeatRestIntegrationTest extends IntegrationTest {

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    TicketRepository ticketRepository;

    private User teamCaptain;
    private String teamCaptainCleartextPassword = "password";

    private Team team;

    private Ticket userTicket;

    private Ticket captainTicket;

    @Before
    public void setupSeatIntegrationTests() {
        List<Seat> seatList = new ArrayList<>(5);

        for (int i = 1; i <= 5; i++) {
            seatList.add(new Seat("A", i));
        }

        seatRepository.save(seatList);

        userTicket = new Ticket(user, TicketType.EARLY_FULL, false, false);
        userTicket.setValid(true);
        userTicket = ticketRepository.saveAndFlush(userTicket);
    }

    @After
    public void cleanupSeatIntegrationTest() {
        seatRepository.deleteAll();
        ticketRepository.deleteAll();
        teamRepository.deleteAll();
    }

    private void setTicketOnA1(Ticket ticket) {
        Seat seat = seatRepository.findAll().get(0);
        seat.setTicket(ticket);

        seatRepository.save(seat);
    }

    private void createCaptainAndTeam() {
        teamCaptain = new User("captain", new BCryptPasswordEncoder().encode(teamCaptainCleartextPassword),
                "captain@mail.com");
        teamCaptain.getProfile()
                .setAllFields("Captain", "Hook", "PeterPanKiller", Gender.MALE, "High Road 3", "2826ZZ", "Neverland",
                        "0906-0777", null);
        teamCaptain = userRepository.saveAndFlush(teamCaptain);

        captainTicket = new Ticket(teamCaptain, TicketType.EARLY_FULL, false, false);
        captainTicket.setValid(true);
        captainTicket = ticketRepository.save(captainTicket);


        team = new Team("team", teamCaptain);
        team.addMember(user);

        team = teamRepository.save(team);
    }

    @Test
    public void getAllSeatAsUser() {
        setTicketOnA1(userTicket);

        SessionData login = login("user", userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/seats").
        then().
            statusCode(HttpStatus.SC_OK).
            body("seatmap.A.ticket.owner", not(contains(hasKey("username")))).
            body("seatmap.A.ticket.owner.profile", contains(hasKey("displayName"))).
            body("seatmap.A", hasSize(5));
        //@formatter:on
    }

    @Test
    public void getAllSeatsAdminViewAsAnon() {
        //@formatter:off
        when().
            get("/seats?admin").
        then().log().all().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void getAllSeatsAdminViewAsUser() {
        setTicketOnA1(userTicket);

        SessionData login = login("user", userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/seats?admin").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void getAllSeatsAdminViewAsAdmin() {
        setTicketOnA1(userTicket);

        SessionData login = login("admin", adminCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/seats?admin").
        then().
            statusCode(HttpStatus.SC_OK).
            body("seatmap.A.ticket.owner", hasItem(hasKey("username"))).
            body("seatmap.A.ticket.owner.profile", contains(hasKey("displayName"))).
            body("seatmap.A.ticket.owner", hasItem(hasKey("authorities"))).
            body("seatmap.A", hasSize(5));
        //@formatter:on
    }

    @Test
    public void getSeatGroupAsUser() {
        setTicketOnA1(userTicket);

        SessionData login = login("user", userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/seats/A").
        then().
            statusCode(HttpStatus.SC_OK).
            body("seatmap.A.ticket.owner", not(contains(hasKey("username")))).
            body("seatmap.A.ticket.owner.profile", contains(hasKey("displayName"))).
            body("seatmap.A", hasSize(5));
        //@formatter:on
    }

    @Test
    public void getSeatGroupAdminViewAsUser() {
        SessionData login = login("user", userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/seats/A?admin").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void getSeatGroupAdminViewAsAdmin() {
        setTicketOnA1(userTicket);

        SessionData login = login("admin", adminCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/seats/A?admin").
        then().
            statusCode(HttpStatus.SC_OK).
            body("seatmap.A.ticket.owner", hasItem(hasKey("username"))).
            body("seatmap.A.ticket.owner.profile", hasItem(hasKey("displayName"))).
            body("seatmap.A.ticket.owner", hasItem(hasKey("authorities"))).
            body("seatmap.A", hasSize(5));
        //@formatter:on
    }

    @Test
    public void getSeatAsUser() {
        setTicketOnA1(userTicket);

        SessionData login = login("user", userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/seats/A/1").
        then().
            statusCode(HttpStatus.SC_OK).
            body("ticket.owner", not(hasKey("username"))).
            body("ticket.owner.profile", hasKey("displayName")).
            body("ticket.owner", not(hasItem(hasKey("authorities"))));
        //@formatter:on
    }

    @Test
    public void getSeatAdminViewAsUser() {
        setTicketOnA1(userTicket);

        SessionData login = login("user", userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/seats/A/1?admin").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void getCurrentSeatAsUser() {
        setTicketOnA1(userTicket);

        SessionData login = login("user", userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/users/current/seat").
        then().
            statusCode(HttpStatus.SC_OK).
            body("[0].ticket.owner.username", is(user.getUsername())).
            body("[0].ticket.owner.profile", hasKey("displayName")).
            body("[0].ticket.owner.profile", hasKey("firstName"));
        //@formatter:on
    }

    @Test
    public void getSeatsForTeam() {
        createCaptainAndTeam();

        setTicketOnA1(captainTicket);

        // Seat User on seat A2
        Seat seat = seatRepository.findAll().get(1);
        seat.setTicket(userTicket);
        seatRepository.save(seat);

        SessionData login = login("user", userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/teams/" + team.getTeamName() + "/seats").
        then().
            statusCode(HttpStatus.SC_OK).
            body("$", hasSize(2)).
            body("ticket.owner.profile.displayName", hasItems(
                    teamCaptain.getProfile().getDisplayName(),
                    user.getProfile().getDisplayName())).
            body("ticket.owner", not(hasKey("username"))).
            body("ticket.owner.profile", not(hasKey("firstName")));
        //@formatter:on
    }
    //endregion

    //region Test Add Seat
    @Test
    public void addSeatGroupAsAnon() {
        Map<String, String> seatGroupDTO = new HashMap<>();
        seatGroupDTO.put("seatGroupName", "testGroup");
        seatGroupDTO.put("numberOfSeats", "5");

        //@formatter:off
        given().
        when().
            content(seatGroupDTO).
            contentType(ContentType.JSON).
            post("/seats").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void addSeatGroupAsUser() {
        Map<String, String> seatGroupDTO = new HashMap<>();
        seatGroupDTO.put("seatGroupName", "testGroup");
        seatGroupDTO.put("numberOfSeats", "5");

        //@formatter:off
        SessionData login = login("user", userCleartextPassword);

        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(seatGroupDTO).
            contentType(ContentType.JSON).
            post("/seats").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void addSeatGroupAsAdmin() {
        Map<String, String> seatGroupDTO = new HashMap<>();
        seatGroupDTO.put("seatGroupName", "testGroup");
        seatGroupDTO.put("numberOfSeats", "5");

        //@formatter:off
        SessionData login = login("admin", adminCleartextPassword);

        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(seatGroupDTO).
            contentType(ContentType.JSON).
            post("/seats").
        then().
            statusCode(HttpStatus.SC_OK);

        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/seats/" + "testGroup").
        then().
            statusCode(HttpStatus.SC_OK).
            body("seatmap.testGroup", hasSize(5));
        //@formatter:on
    }
    //endregion

    //region Test Reserve Seat
    @Test
    public void reserveSeatAsAnon() {
        //@formatter:off
        given().
        when().
            param("username", "user").
            post("/seats/A/1").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void reserveSeatAsUser() {
        SessionData login = login("user", userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            param("ticketId", userTicket.getId()).
            post("/seats/A/1").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void reserveSeatAsOtherUser() {
        SessionData login = login("user", userCleartextPassword);

        createCaptainAndTeam();

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            param("ticketId", captainTicket.getId()).
            post("/seats/A/1").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void reserveSeatForUserAsTeamCaptain() {
        createCaptainAndTeam();

        SessionData login = login("captain", teamCaptainCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            param("ticketId", userTicket.getId()).
            post("/seats/A/1").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void reserveSeatForUserAsAdmin() {
        createCaptainAndTeam();

        SessionData login = login("admin", adminCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            param("ticketId", userTicket.getId()).
            post("/seats/A/1").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void reserveTakenSeatAsUser() {
        createCaptainAndTeam();

        SessionData login = login("captain", teamCaptainCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            param("ticketId", captainTicket.getId()).
            post("/seats/A/1").
        then().
            statusCode(HttpStatus.SC_OK);

        logout();

        SessionData login2 = login("user", userCleartextPassword);

        given().
            filter(sessionFilter).
            header(login2.getCsrfHeader()).
        when().
            param("ticketId", userTicket.getId()).
            post("/seats/A/1").
        then().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on
    }

    @Test
    public void reserveSeatWithInvalidTicket() {
        userTicket.setValid(false);
        ticketRepository.save(userTicket);

        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            param("ticketId", userTicket.getId()).
            post("/seats/A/1").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void changeSeatAsUser() {
        SessionData login = login("user", userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            param("ticketId", userTicket.getId()).
            post("/seats/A/1").
        then().
            statusCode(HttpStatus.SC_OK);

        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            param("ticketId", userTicket.getId()).
            post("/seats/A/2").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Seat previousSeat = seatRepository.findBySeatGroupAndSeatNumber("A", 1);
        Seat currentSeat = seatRepository.findBySeatGroupAndSeatNumber("A", 2);

        Assert.assertNull(previousSeat.getTicket());
        Assert.assertFalse(previousSeat.isTaken());
        Assert.assertEquals(currentSeat.getTicket().getId(), userTicket.getId());
        Assert.assertTrue(currentSeat.isTaken());
    }

    @Test
    public void changeSeatAsAdmin() {
        SessionData login = login("user", userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            param("ticketId", userTicket.getId()).
            post("/seats/A/1").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        logout();

        login = login("admin", adminCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            param("ticketId", userTicket.getId()).
            post("/seats/A/2").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void changeSeatAsCaptain() {
        createCaptainAndTeam();

        SessionData login = login("user", userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            param("ticketId", userTicket.getId()).
            post("/seats/A/1").
        then().
            statusCode(HttpStatus.SC_OK);

        logout();

        login = login("captain", teamCaptainCleartextPassword);

        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            param("ticketId", userTicket.getId()).
            post("/seats/A/2").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }
    //endregion

    @Test
    public void clearSeatAsAnon() {
        setTicketOnA1(userTicket);

        //@formatter:off
        given().
        when().
            delete("/seats/A/1").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void clearSeatAsUser() {
        setTicketOnA1(userTicket);
        SessionData login = login("user", userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            delete("/seats/A/1").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void clearSeatAsAdmin() {
        setTicketOnA1(userTicket);
        SessionData login = login(admin.getUsername(), adminCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            delete("/seats/A/1").
        then().log().all().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Seat seat = seatRepository.findBySeatGroupAndSeatNumber("A", 1);
        Assert.assertNull(seat.getTicket());
    }
}
