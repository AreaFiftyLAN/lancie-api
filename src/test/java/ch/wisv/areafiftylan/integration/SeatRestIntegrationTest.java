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


package ch.wisv.areafiftylan.integration;

import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.service.repository.TicketRepository;
import ch.wisv.areafiftylan.seats.model.Seat;
import ch.wisv.areafiftylan.seats.service.SeatRepository;
import ch.wisv.areafiftylan.seats.service.SeatService;
import ch.wisv.areafiftylan.teams.model.Team;
import ch.wisv.areafiftylan.users.model.User;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class SeatRestIntegrationTest extends XAuthIntegrationTest {
    
    private final String SEAT_ENDPOINT = "/seats";
    private final String LOCK_ENDPOINT = SEAT_ENDPOINT + "/lock/";

    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SeatService seatService;

    private void setTicketOnA1(Ticket ticket) {
        Seat seat = seatRepository.findAll().get(0);
        seat.setTicket(ticket);

        seatRepository.save(seat);
    }

    @After
    public void cleanupSeatTest() {
        seatService.clearSeat("A", 1);
        seatService.clearSeat("A", 2);
        seatService.clearSeat("A", 3);
        seatService.clearSeat("A", 4);
        seatService.clearSeat("A", 5);
        seatService.setAllSeatsLock(false);
    }

    //region Get Seat
    @Test
    public void getAllSeatAsUser() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        setTicketOnA1(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(SEAT_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("seatmap.A.ticket.owner", not(contains(hasKey("email")))).
            body("seatmap.A.ticket.owner.profile", contains(hasKey("displayName"))).
            body("seatmap.A", hasSize(5));
        //@formatter:on
    }

    @Test
    public void getAllSeatsAdminViewAsAnon() {
        //@formatter:off
        given().
            param("admin", true).
        when().
            get(SEAT_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void getAllSeatsAdminViewAsUser() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        setTicketOnA1(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            param("admin", true).
        when().
            get(SEAT_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("seatmap.A.ticket.owner", not(contains(hasKey("email")))).
            body("seatmap.A.ticket.owner.profile", contains(hasKey("displayName"))).
            body("seatmap.A", hasSize(5));
        //@formatter:on
    }

    @Test
    public void getAllSeatsAdminViewAsAdmin() {
        User admin = createAdmin();
        Ticket ticket = createTicketForUser(admin);
        setTicketOnA1(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
            param("admin", true).
        when().
            get(SEAT_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("seatmap.A.ticket.owner", hasItem(hasKey("email"))).
            body("seatmap.A.ticket.owner.profile", contains(hasKey("displayName"))).
            body("seatmap.A.ticket.owner", hasItem(hasKey("authorities"))).
            body("seatmap.A", hasSize(5));
        //@formatter:on
    }

    @Test
    public void getSeatGroupAsUser() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        setTicketOnA1(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(SEAT_ENDPOINT + "/A").
        then().
            statusCode(HttpStatus.SC_OK).
            body("seatmap.A.ticket.owner", not(contains(hasKey("email")))).
            body("seatmap.A.ticket.owner.profile", contains(hasKey("displayName"))).
            body("seatmap.A", hasSize(5));
        //@formatter:on
    }

    @Test
    public void getSeatGroupAdminViewAsUser() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        setTicketOnA1(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            param("admin", true).
        when().
            get(SEAT_ENDPOINT + "/A").
        then().
            statusCode(HttpStatus.SC_OK).
            body("seatmap.A.ticket.owner", not(contains(hasKey("email")))).
            body("seatmap.A.ticket.owner.profile", contains(hasKey("displayName"))).
            body("seatmap.A", hasSize(5));
        //@formatter:on
    }

    @Test
    public void getSeatGroupAdminViewAsAdmin() {
        User admin = createAdmin();
        Ticket ticket = createTicketForUser(admin);
        setTicketOnA1(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
            param("admin", true).
        when().
            get(SEAT_ENDPOINT + "/A").
        then().
            statusCode(HttpStatus.SC_OK).
            body("seatmap.A.ticket.owner", hasItem(hasKey("email"))).
            body("seatmap.A.ticket.owner.profile", hasItem(hasKey("displayName"))).
            body("seatmap.A.ticket.owner", hasItem(hasKey("authorities"))).
            body("seatmap.A", hasSize(5));
        //@formatter:on
    }

    @Test
    public void getSeatAsUser() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        setTicketOnA1(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(SEAT_ENDPOINT + "/A/1").
        then().
            statusCode(HttpStatus.SC_OK).
            body("ticket.owner", not(hasKey("email"))).
            body("ticket.owner.profile", hasKey("displayName")).
            body("ticket.owner", not(hasItem(hasKey("authorities"))));
        //@formatter:on
    }

    @Test
    public void getSeatAdminViewAsUser() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        setTicketOnA1(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            param("admin", true).
        when().
            get(SEAT_ENDPOINT + "/A/1").
        then().
            statusCode(HttpStatus.SC_OK).
            body("ticket.owner", not(hasKey("email"))).
            body("ticket.owner.profile", hasKey("displayName")).
            body("ticket.owner", not(hasItem(hasKey("authorities"))));
        //@formatter:on
    }

    @Test
    public void getSeatAdminViewAsAdmin() {
        User admin = createAdmin();
        Ticket ticket = createTicketForUser(admin);
        setTicketOnA1(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
            param("admin", true).
        when().
            get(SEAT_ENDPOINT + "/A/1").
        then().
            statusCode(HttpStatus.SC_OK).
            body("ticket.owner", hasKey("email")).
            body("ticket.owner.profile", hasKey("displayName")).
            body("ticket.owner", hasKey("authorities"));
        //@formatter:on
    }

    @Test
    public void getCurrentSeatAsUser() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        setTicketOnA1(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get("/users/current/seat").
        then().
            statusCode(HttpStatus.SC_OK).
            body("[0].ticket.owner.email", is(user.getEmail())).
            body("[0].ticket.owner.profile", hasKey("displayName")).
            body("[0].ticket.owner.profile", hasKey("firstName"));
        //@formatter:on
    }

    @Test
    public void getSeatsForTeam() {
        User captain = createUser();
        Ticket captainTicket = createTicketForUser(captain);
        User user = createUser();
        Ticket userTicket = createTicketForUser(user);
        Team team = createTeamWithCaptain(captain);
        addMemberToTeam(team, user);
        setTicketOnA1(captainTicket);

        // Seat User on seat A2
        Seat seat = seatRepository.findAll().get(1);
        seat.setTicket(userTicket);
        seatRepository.save(seat);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get("/teams/" + team.getTeamName() + SEAT_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("$", hasSize(2)).
            body("ticket.owner.profile.displayName", hasItems(
                    captain.getProfile().getDisplayName(),
                    user.getProfile().getDisplayName())).
            body("ticket.owner", not(hasKey("email"))).
            body("ticket.owner.profile", not(hasKey("firstName")));
        //@formatter:on
    }
    //endregion Get Seat
    //region Add Seat
    @Test
    public void addSeatGroupAsAnon() {
        Map<String, String> seatGroupDTO = new HashMap<>();
        seatGroupDTO.put("seatGroupName", "testGroup");
        seatGroupDTO.put("numberOfSeats", "5");

        //@formatter:off
        given().
            body(seatGroupDTO).
            contentType(ContentType.JSON).
        when().
            post(SEAT_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void addSeatGroupAsUser() {
        User user = createUser();
        Map<String, String> seatGroupDTO = new HashMap<>();
        seatGroupDTO.put("seatGroupName", "testGroup");
        seatGroupDTO.put("numberOfSeats", "5");

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(seatGroupDTO).
            contentType(ContentType.JSON).
        when().
            post(SEAT_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void addSeatGroupAsAdmin() {
        User admin = createAdmin();
        Map<String, String> seatGroupDTO = new HashMap<>();
        seatGroupDTO.put("seatGroupName", "testGroup");
        seatGroupDTO.put("numberOfSeats", "5");

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            body(seatGroupDTO).
            contentType(ContentType.JSON).
            post(SEAT_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        List<Seat> seatGroup = seatRepository.findBySeatGroup(seatGroupDTO.get("seatGroupName"));
        assertTrue(seatGroup.size() == 5);
    }
    //endregion
    //region Reserve Seat
    @Test
    public void reserveSeatAsAnon() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        //@formatter:off
        given().
        when().
            param("ticketId", ticket.getId()).
            post(SEAT_ENDPOINT + "/A/1").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void reserveSeatAsUser() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            post(SEAT_ENDPOINT + "/A/1/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void reserveSeatAsOtherUser() {
        User user1 = createUser();
        Ticket ticket = createTicketForUser(user1);
        User user2 = createUser();
        createTicketForUser(user2);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user2)).
        when().
            post(SEAT_ENDPOINT + "/A/1/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void reserveSeatForUserAsTeamCaptain() {
        User captain = createUser();
        Ticket captainTicket = createTicketForUser(captain);
        User user = createUser();
        Ticket userTicket = createTicketForUser(user);
        Team team = createTeamWithCaptain(captain);
        addMemberToTeam(team, user);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            post(SEAT_ENDPOINT + "/A/1/" + userTicket.getId()).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void reserveSeatForUserAsAdmin() {
        User user = createUser();
        Ticket userTicket = createTicketForUser(user);
        User admin = createAdmin();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            post(SEAT_ENDPOINT + "/A/1/" + userTicket.getId()).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void reserveTakenSeatAsUser() {
        User user1 = createUser();
        Ticket ticket1 = createTicketForUser(user1);
        setTicketOnA1(ticket1);

        User user2 = createUser();
        Ticket ticket2 = createTicketForUser(user2);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user2)).
        when().
            post(SEAT_ENDPOINT + "/A/1/" + ticket2.getId()).
        then().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on
    }

    @Test
    public void reserveTakenSeatAsAdmin() {
        User user1 = createUser();
        Ticket ticket1 = createTicketForUser(user1);
        setTicketOnA1(ticket1);

        User user2 = createAdmin();
        Ticket ticket2 = createTicketForUser(user2);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user2)).
        when().
            post(SEAT_ENDPOINT + "/A/1/" + ticket2.getId()).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void reserveSeatWithInvalidTicket() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        ticket.setValid(false);
        ticketRepository.save(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            post(SEAT_ENDPOINT + "/A/1/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void changeSeatAsUser() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        setTicketOnA1(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            post(SEAT_ENDPOINT + "/A/2/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Seat previousSeat = seatService.getSeatBySeatGroupAndSeatNumber("A", 1);
        Seat currentSeat = seatService.getSeatBySeatGroupAndSeatNumber("A", 2);

        Assert.assertNull(previousSeat.getTicket());
        Assert.assertFalse(previousSeat.isTaken());
        Assert.assertEquals(currentSeat.getTicket().getId(), ticket.getId());
        assertTrue(currentSeat.isTaken());
    }

    @Test
    public void changeSeatAsAdmin() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        setTicketOnA1(ticket);
        User admin = createAdmin();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            post(SEAT_ENDPOINT + "/A/2/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void changeSeatAsCaptain() {
        User captain = createUser();
        Ticket captainTicket = createTicketForUser(captain);
        User user = createUser();
        Ticket userTicket = createTicketForUser(user);
        Team team = createTeamWithCaptain(captain);
        addMemberToTeam(team, user);
        setTicketOnA1(userTicket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(captain)).
        when().
            post(SEAT_ENDPOINT + "/A/2/" + userTicket.getId()).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }
    //endregion
    //region Clear seat
    @Test
    public void clearSeatAsAnon() {
        Ticket ticket = createTicketForUser(createUser());
        setTicketOnA1(ticket);

        //@formatter:off
        given().
        when().
            delete(SEAT_ENDPOINT + "/A/1").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void clearSeatAsUser() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        setTicketOnA1(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            delete(SEAT_ENDPOINT + "/A/1").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void clearSeatAsAdmin() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        setTicketOnA1(ticket);
        User admin = createAdmin();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            delete(SEAT_ENDPOINT + "/A/1").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Seat seat = seatService.getSeatBySeatGroupAndSeatNumber("A", 1);
        Assert.assertNull(seat.getTicket());
    }

    @Test
    public void reserveSeatAsAnonWithoutTicket() {
        Ticket ticket = createTicketForUser(createUser());
        setTicketOnA1(ticket);

        //@formatter:off
        given().
        when().
            post(SEAT_ENDPOINT + "/A/1").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void reserveSeatAsUserWithoutTicket() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        setTicketOnA1(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            post(SEAT_ENDPOINT + "/A/1").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void reserveSeatAsAdminWithoutTicket() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        setTicketOnA1(ticket);
        User admin = createAdmin();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            post(SEAT_ENDPOINT + "/A/1").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Seat seat = seatService.getSeatBySeatGroupAndSeatNumber("A", 1);
        Assert.assertNull(seat.getTicket());
    }
    //endregion Clear seat
    //region Lock Seat
    @Test
    public void setAllSeatsLockFalse() {
        User admin = createAdmin();

        //formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
            body(false).
            contentType(ContentType.JSON).
        when().
            post(LOCK_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK);
        //formatter:on

        assertTrue(seatService.getAllSeats().getSeatmap().values()
                .stream()
                .flatMap(Collection::stream)
                .noneMatch(Seat::isLocked));
    }

    @Test
    public void setAllSeatsLockTrue() {
        User admin = createAdmin();

        //formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
            body(true).
            contentType(ContentType.JSON).
        when().
            post(LOCK_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK);
        //formatter:on

        assertTrue(seatService.getAllSeats().getSeatmap().values()
                .stream()
                .flatMap(Collection::stream)
                .allMatch(Seat::isLocked));
    }

    @Test
    public void setSeatGroupLockTestFalse() {
        User admin = createAdmin();
        String group = "A";

        //formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
            body(false).
            contentType(ContentType.JSON).
        when().
            post(LOCK_ENDPOINT + group).
        then().
            statusCode(HttpStatus.SC_OK);
        //formatter:on

        assertTrue(seatService.getAllSeats().getSeatmap().get(group)
                .stream()
                .noneMatch(Seat::isLocked));
    }

    @Test
    public void setSeatGroupLockTestTrue() {
        User admin = createAdmin();
        String group = "A";

        //formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
            body(true).
            contentType(ContentType.JSON).
        when().
            post(LOCK_ENDPOINT + group).
        then().
            statusCode(HttpStatus.SC_OK);
        //formatter:on

        assertTrue(seatService.getAllSeats().getSeatmap().get(group)
                .stream()
                .allMatch(Seat::isLocked));
    }

    @Test
    public void setSeatLockTestFalse() {
        User admin = createAdmin();
        String group = "A";

        //formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
            body(false).
            contentType(ContentType.JSON).
        when().
            post(LOCK_ENDPOINT + group + "/3").
        then().
            statusCode(HttpStatus.SC_OK);
        //formatter:on

        assertFalse(seatService.getAllSeats().getSeatmap().get(group).get(2).isLocked());
    }

    @Test
    public void setSeatLockTestTrue() {
        User admin = createAdmin();
        String group = "A";

        //formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
            body(true).
            contentType(ContentType.JSON).
        when().
            post(LOCK_ENDPOINT + group + "/3").
        then().
            statusCode(HttpStatus.SC_OK);
        //formatter:on

        assertTrue(seatService.getAllSeats().getSeatmap().get(group).get(2).isLocked());
    }
    //endregion Lock Seat
}
