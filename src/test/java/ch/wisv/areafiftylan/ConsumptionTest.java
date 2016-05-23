package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.ConsumptionMap;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.util.Consumption;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.service.ConsumptionService;
import ch.wisv.areafiftylan.service.repository.ConsumptionMapsRepository;
import ch.wisv.areafiftylan.service.repository.PossibleConsumptionsRepository;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import ch.wisv.areafiftylan.util.SessionData;
import com.jayway.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
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
    private ConsumptionMapsRepository consumptionMapsRepository;

    @Autowired
    private PossibleConsumptionsRepository possibleConsumptionsRepository;

    @Autowired
    private TicketRepository ticketRepository;

    private Consumption spicyFood;
    private Consumption coldMilkshake;

    private Ticket ticket;

    @Before
    public void InitConsumptionTest(){
        spicyFood = consumptionService.addPossibleConsumption("Nice Spicy Food");
        coldMilkshake = consumptionService.addPossibleConsumption("Deliciously Cold Milkshake");
        ticket = makeTicket();
    }

    private Ticket makeTicket() {
        Ticket t = new Ticket(user, TicketType.EARLY_FULL, false, false);
        t.setValid(true);
        return ticketRepository.saveAndFlush(t);
    }

    @After
    public void consumptionTestCleanup(){
        consumptionMapsRepository.deleteAll();
        possibleConsumptionsRepository.deleteAll();
        ticketRepository.deleteAll();
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
        consumptionService.removePossibleConsumption(spicyFood.getId());
        consumptionService.removePossibleConsumption(coldMilkshake.getId());

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
        consumptionService.removePossibleConsumption(spicyFood.getId());

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
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        consumptionService.consume(ticket.getId(), spicyFood.getId());

        Map<String, String> seatGroupDTO = makeConsumptionRequestBody(ticket.getId(), spicyFood.getId());

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(seatGroupDTO).
                contentType(ContentType.JSON).
                get(CONSUMPTION_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_OK).
                body("consumed", is(true));
    }

    @Test
    public void getIsConsumed_False(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        Map<String, String> seatGroupDTO = makeConsumptionRequestBody(ticket.getId(), spicyFood.getId());

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(seatGroupDTO).
                contentType(ContentType.JSON).
                get(CONSUMPTION_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_OK).
                body("consumed", is(false));
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
    public void consumeConsumption_InvalidTicket(){

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
    public void resetConsumption_InvalidTicket(){

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

    @Test
    public void removePossibleConsumption_DoesntExist(){

    }

    private Map<String, String> makeConsumptionRequestBody(Long ticketId, Long consumptionId){
        Map<String, String> consumptionDTO = new HashMap<>();
        consumptionDTO.put("ticketId", ticketId.toString());
        consumptionDTO.put("consumptionId", consumptionId.toString());
        return consumptionDTO;
    }
}
