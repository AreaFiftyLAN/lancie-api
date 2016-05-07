package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.service.repository.RFIDLinkRepository;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;

/**
 * Created by beer on 7-5-16.
 */
public class RFIDTest extends IntegrationTest {
    private final String RFID_ENDPOINT = "/rfid";

    @Autowired
    private RFIDLinkRepository rfidLinkRepository;

    @Before
    public void RFIDTestInit(){

    }

    @After
    public void RFIDTestCleanup(){
        rfidLinkRepository.deleteAll();
    }

    @Test
    public void testGetRFIDLinks_Anon(){
        when().get(RFID_ENDPOINT).
                then().statusCode(HttpStatus.SC_FORBIDDEN);
    }
}
