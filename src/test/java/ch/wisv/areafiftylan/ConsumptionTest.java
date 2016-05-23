package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.ConsumptionMap;
import ch.wisv.areafiftylan.model.util.Consumption;
import ch.wisv.areafiftylan.service.ConsumptionService;
import ch.wisv.areafiftylan.service.repository.ConsumptionRepository;
import ch.wisv.areafiftylan.service.repository.PossibleConsumptionsRepository;
import ch.wisv.areafiftylan.util.SessionData;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

/**
 * Created by beer on 20-5-16.
 */
public class ConsumptionTest extends IntegrationTest {
    private final String CONSUMPTION_ENDPOINT = "/consumptions";
    private final String CONSUMPTIONS_POSSIBLE_ENDPOINT = CONSUMPTION_ENDPOINT + "/available";

    @Autowired
    private ConsumptionService consumptionService;

    @Autowired
    private ConsumptionRepository consumptionRepository;

    @Autowired
    private PossibleConsumptionsRepository possibleConsumptionsRepository;

    private Consumption spicyFood = new Consumption("Nice Spicy Food");
    private Consumption coldMilkshake = new Consumption("Deliciously Cold Milkshake");

    @Before
    public void InitConsumptionTest(){
        ConsumptionMap.PossibleConsumptions = possibleConsumptionsRepository.findAll();
        consumptionService.addPossibleConsumption(spicyFood);
        consumptionService.addPossibleConsumption(coldMilkshake);
    }

    @After
    public void consumptionTestCleanup(){
        consumptionRepository.deleteAll();
        possibleConsumptionsRepository.deleteAll();
    }

    @Test
    public void getAllPossibleConsumptionsTestNone_NoAdmin(){
        SessionData session = login(user.getUsername(), userCleartextPassword);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                get(CONSUMPTIONS_POSSIBLE_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void getAllPossibleConsumptionsTestNone_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);
        consumptionService.removePossibleConsumption(spicyFood);
        consumptionService.removePossibleConsumption(coldMilkshake);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                get(CONSUMPTIONS_POSSIBLE_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(0));
    }

    @Test
    public void getAllPossibleConsumptionsTestSingle_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);
        consumptionService.removePossibleConsumption(spicyFood);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                get(CONSUMPTIONS_POSSIBLE_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(1)).
                body("$", containsInAnyOrder(coldMilkshake.getName()));
    }

    @Test
    public void getAllPossibleConsumptionsTestMultiple_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                get(CONSUMPTIONS_POSSIBLE_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(2)).
                body("$", containsInAnyOrder(coldMilkshake.getName(), spicyFood.getName()));
    }

    @Test
    public void getIsConsumed_True(){

    }

    @Test
    public void getIsConsumed_False(){

    }

    @Test
    public void getTicketConsumptions_NoneConsumed(){

    }

    @Test
    public void getTicketConsumptions_OneConsumed(){

    }

    @Test
    public void getTicketConsumptions_MultipleConsumed(){

    }

    @Test
    public void consumeConsumption(){

    }

    @Test
    public void consumeConsumption_AlreadyConsumer(){

    }

    @Test
    public void consumeConsumption_ConsumptionDoesntExist(){

    }

    @Test
    public void consumeConsumption_ConsumptionDeleted(){

    }

    @Test
    public void resetConsumption(){

    }

    @Test
    public void resetConsumption_ConsumptionDoesntExist(){

    }

    @Test
    public void resetConsumption_ConsumptionDeleted(){

    }

    @Test
    public void addPossibleConsumption(){

    }

    @Test
    public void addPossibleConsumption_Duplicate(){

    }

    @Test
    public void removePossibleConsumption(){

    }

    public void removePossibleConsumption_DoesntExist(){

    }
}
