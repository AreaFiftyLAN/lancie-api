package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.Seat;
import ch.wisv.areafiftylan.service.repository.SeatRespository;
import ch.wisv.areafiftylan.util.SessionData;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Before
    public void setupSeatIntegrationTests() {
        List<Seat> seatList = new ArrayList<>(19);

        Seat seat = new Seat("A", 1);
        seat.setUser(user);

        for (int i = 1; i <= 20; i++) {
            seatList.add(new Seat("A", i));
        }

        seatRespository.save(seat);
        seatRespository.save(seatList);
    }

    @After
    public void cleanupSeatIntegrationTest() {
        seatRespository.deleteAll();
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
        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/seats").
        then().log().all().
            statusCode(HttpStatus.SC_OK).
            body("seatmap.A.user", not(hasItem("username"))).
            body("seatmap.A.user.profile", hasItem("displayName")).
            body("seatmap.A", arrayWithSize(20));
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
        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/seats?admin").
        then().log().all().
            statusCode(HttpStatus.SC_OK).
            body("seatmap.A.user", hasItem("username")).
            body("seatmap.A.user.profile", hasItem("displayName")).
            body("seatmap.A", arrayWithSize(20));
        //@formatter:on
    }

    @Test
    public void getSeatGroupAsUser(){

    }

    @Test
    public void getSeatGroupAdminViewAsUser(){

    }

    @Test
    public void getSeatGroupAdminViewAsAdmin(){

    }

    @Test
    public void getSeatAsUser(){

    }

    @Test
    public void getSeatAdminViewAsUser(){

    }

    @Test
    public void getCurrentSeatAsUser(){

    }

    @Test
    public void getSeatsForTeam(){

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
