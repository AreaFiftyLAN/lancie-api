package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Gender;
import ch.wisv.areafiftylan.service.repository.SeatRespository;
import ch.wisv.areafiftylan.service.repository.TeamRepository;
import ch.wisv.areafiftylan.util.SessionData;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

/**
 * Created by sille on 17-11-15.
 */
public class SeatRestIntegrationTest extends IntegrationTest {

    @Autowired
    SeatRespository seatRespository;

    @Autowired
    TeamRepository teamRepository;

    User teamCaptain;

    Team team;

    @Before
    public void setupSeatIntegrationTests() {
        List<Seat> seatList = new ArrayList<>(5);

        for (int i = 1; i <= 5; i++) {
            seatList.add(new Seat("A", i));
        }

        seatRespository.save(seatList);
    }

    @After
    public void cleanupSeatIntegrationTest() {
        seatRespository.deleteAll();
        teamRepository.deleteAll();
    }

    private void setUserOnA1(User user) {
        Seat seat = seatRespository.findAll().get(0);
        seat.setUser(user);

        seatRespository.save(seat);
    }

    private void createCaptainAndTeam() {
        teamCaptain = new User("captain", new BCryptPasswordEncoder().encode("password"), "captain@mail.com");
        teamCaptain.getProfile()
                .setAllFields("Captain", "Hook", "PeterPanKiller", Gender.MALE, "High Road 3", "2826ZZ", "Neverland",
                        "0906-0777", null);

        teamCaptain = userRepository.saveAndFlush(teamCaptain);

        team = new Team("team", teamCaptain);
        team.addMember(user);

        team = teamRepository.save(team);
    }

    @Test
    public void getAllSeatAsAnon() {
        //@formatter:off
        when().
            get("/seats").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void getAllSeatAsUser() {
        setUserOnA1(user);

        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/seats").
        then().log().all().
            statusCode(HttpStatus.SC_OK).
            body("seatmap.A.user", not(contains(hasKey("username")))).
            body("seatmap.A.user.profile", contains(hasKey("displayName"))).
            body("seatmap.A", hasSize(5));
        //@formatter:on
    }

    @Test
    public void getAllSeatsAdminViewAsAnon(){
        //@formatter:off
        when().
            get("/seats?admin").
        then().log().all().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void getAllSeatsAdminViewAsUser(){
        setUserOnA1(user);

        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/seats?admin").
        then().log().all().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void getAllSeatsAdminViewAsAdmin(){
        setUserOnA1(user);

        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/seats?admin").
        then().
            statusCode(HttpStatus.SC_OK).
            body("seatmap.A.user", hasItem(hasKey("username"))).
            body("seatmap.A.user.profile", contains(hasKey("displayName"))).
            body("seatmap.A.user", hasItem(hasKey("authorities"))).
            body("seatmap.A", hasSize(5));
        //@formatter:on
    }

    @Test
    public void getSeatGroupAsUser(){
        setUserOnA1(user);

        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/seats/A").
        then().log().all().
            statusCode(HttpStatus.SC_OK).
            body("seatmap.A.user", not(contains(hasKey("username")))).
            body("seatmap.A.user.profile", contains(hasKey("displayName"))).
            body("seatmap.A", hasSize(5));
        //@formatter:on
    }

    @Test
    public void getSeatGroupAdminViewAsUser(){
        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/seats/A?admin").
        then().log().all().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void getSeatGroupAdminViewAsAdmin(){
        setUserOnA1(user);

        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/seats/A?admin").
        then().
            statusCode(HttpStatus.SC_OK).
            body("seatmap.A.user", hasItem(hasKey("username"))).
            body("seatmap.A.user.profile", hasItem(hasKey("displayName"))).
            body("seatmap.A.user", hasItem(hasKey("authorities"))).
            body("seatmap.A", hasSize(5));
        //@formatter:on
    }

    @Test
    public void getSeatAsUser(){
        setUserOnA1(user);

        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/seats/A/1").
        then().
            statusCode(HttpStatus.SC_OK).
            body("user", not(hasKey("username"))).
            body("user.profile", hasKey("displayName")).
            body("user", not(hasItem(hasKey("authorities"))));
        //@formatter:on
    }

    @Test
    public void getSeatAdminViewAsUser(){
        setUserOnA1(user);

        SessionData login = login("user");

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
    public void getCurrentSeatAsUser(){
        setUserOnA1(user);

        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/users/current/seat").
        then().
            statusCode(HttpStatus.SC_OK).
            body("user.username", is("user")).
            body("user.profile", hasKey("displayName")).
            body("user.profile", hasKey("firstName"));
        //@formatter:on
    }

    @Test
    public void getSeatsForTeam(){
        createCaptainAndTeam();

        setUserOnA1(teamCaptain);

        // Seat User on seat A2
        Seat seat = seatRespository.findAll().get(1);
        seat.setUser(user);
        seatRespository.save(seat);

        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/teams/" + team.getTeamName() + "/seats").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void addSeatGroupAsAnon(){

    }

    @Test
    public void addSeatGroupAsUser(){

    }

    @Test
    public void addSeatGroupAsAdmin(){

    }

    @Test
    public void reserveSeatAsAnon(){
        //@formatter:off
        given().
        when().
            param("username", "user").
            post("/seats/A/1").
        then().log().all().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void reserveSeatAsUser(){
        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            param("username", "user").
            post("/seats/A/1").
        then().log().all().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void reserveSeatAsOtherUser(){
        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            param("username", "admin").
            post("/seats/A/1").
        then().log().all().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void reserveSeatAsTeamCaptain(){

    }

    @Test
    public void reserveSeatAsAdmin(){

    }

    @Test
    public void reserveTakenSeatAsUser(){

    }

    @Test
    public void clearSeatAsAnon(){

    }

    @Test
    public void clearSeatAsUser(){

    }

    @Test
    public void clearSeatAsAdmin(){

    }

    @Test
    public void changeSeatAsUser(){

    }

    @Test
    public void changeSeatAsAdmin(){

    }

    @Test
    public void changeSeatAsCaptain(){

    }


}
