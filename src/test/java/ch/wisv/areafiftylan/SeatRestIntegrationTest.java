package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.service.repository.SeatRespository;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.jayway.restassured.RestAssured.when;

/**
 * Created by sille on 17-11-15.
 */
public class SeatRestIntegrationTest extends TeamRestIntegrationTest {

    @Autowired
    SeatRespository seatRespository;


    @Test
    public void testGetSingleSeatAnon() {
        //@formatter:off
        when().get("/seats").
                then().statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }


}
