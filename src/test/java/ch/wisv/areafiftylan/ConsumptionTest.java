package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.util.Consumption;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.service.ConsumptionService;
import ch.wisv.areafiftylan.service.repository.ConsumptionMapsRepository;
import ch.wisv.areafiftylan.service.repository.PossibleConsumptionsRepository;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import ch.wisv.areafiftylan.util.SessionData;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

/**
 * Created by beer on 20-5-16.
 */
public class ConsumptionTest extends IntegrationTest {
    private final String CONSUMPTION_ENDPOINT = "/consumptions";

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
    private final String UNUSED_CONSUMPTION_NAME = "Hot Chocolate";

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

        getAllPossibleConsumptions(session).
            then().
                statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void getAllPossibleConsumptionsTestNone_Admin(){
        consumptionService.removePossibleConsumption(spicyFood.getId());
        consumptionService.removePossibleConsumption(coldMilkshake.getId());


        getAllPossibleConsumptions_AsAdmin().
            then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(0));
    }

    @Test
    public void getAllPossibleConsumptionsTestSingle_Admin(){
        consumptionService.removePossibleConsumption(spicyFood.getId());

        getAllPossibleConsumptions_AsAdmin().
            then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(1)).
                body("name", containsInAnyOrder(coldMilkshake.getName()));
    }

    @Test
    public void getAllPossibleConsumptionsTestMultiple_Admin(){
        getAllPossibleConsumptions_AsAdmin().
            then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(2)).
                body("name", containsInAnyOrder(coldMilkshake.getName(), spicyFood.getName()));
    }

    @Test
    public void getTicketConsumptions_NoneConsumed(){
        getTicketConsumptions(ticket).
            then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(0));
    }

    @Test
    public void getTicketConsumptions_OneConsumed(){
        consumptionService.consume(ticket.getId(), spicyFood.getId());

        getTicketConsumptions(ticket).
            then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(1)).
                body("name", containsInAnyOrder(spicyFood.getName()));
    }

    @Test
    public void getTicketConsumptions_OneConsumedAndDeleted(){
        consumptionService.consume(ticket.getId(), spicyFood.getId());
        consumptionService.removePossibleConsumption(spicyFood.getId());

        getTicketConsumptions(ticket).
            then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(0));
    }

    @Test
    public void consumeConsumption(){
        consumeConsumption(ticket, spicyFood).
                then().statusCode(HttpStatus.SC_OK);

        Assert.assertTrue(consumptionService.isConsumed(ticket.getId(), spicyFood.getId()));
    }

    @Test
    public void consumeConsumption_AlreadyConsumed(){
        consumeConsumption(ticket, spicyFood);
        consumeConsumption(ticket, spicyFood).
                then().statusCode(HttpStatus.SC_CONFLICT);
    }

    @Test
    public void consumeConsumption_ConsumptionDoesntExist(){
        consumeConsumption(ticket.getId(), getUnusedConsumptionId()).
                then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void consumeConsumption_ConsumptionDeleted(){
        consumptionService.removePossibleConsumption(spicyFood.getId());
        consumeConsumption(ticket, spicyFood).
                then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void consumeConsumption_InvalidTicket(){
        invalidateTicket();

        consumeConsumption(ticket, spicyFood).
                then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void resetConsumption(){
        consumeAndResetConsumption(ticket, spicyFood).
                then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void resetConsumption_ConsumptionDoesntExist(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        resetConsumption(ticket.getId(), getUnusedConsumptionId(), session).
                then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void resetConsumption_ConsumptionDeleted(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        consumptionService.consume(ticket.getId(), spicyFood.getId());

        consumptionService.removePossibleConsumption(spicyFood.getId());

        resetConsumption(ticket.getId(), spicyFood.getId(), session).
                then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void resetConsumption_InvalidTicket(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        consumptionService.consume(ticket.getId(), spicyFood.getId());

        invalidateTicket();

        resetConsumption(ticket.getId(), spicyFood.getId(), session).
                then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void addPossibleConsumption(){
        addPossibleConsumption(UNUSED_CONSUMPTION_NAME)
                .then().statusCode(HttpStatus.SC_OK);

        boolean consumptionExists =
                consumptionService.getPossibleConsumptions()
                        .stream()
                        .anyMatch(c -> c.getName().equals(UNUSED_CONSUMPTION_NAME));
        Assert.assertTrue(consumptionExists);
    }

    @Test
    public void addPossibleConsumption_Duplicate(){
        addPossibleConsumption(UNUSED_CONSUMPTION_NAME);
        addPossibleConsumption(UNUSED_CONSUMPTION_NAME)
                .then().statusCode(HttpStatus.SC_CONFLICT);

    }

    @Test
    public void removePossibleConsumption(){
        removePossibleConsumption(spicyFood)
                .then().statusCode(HttpStatus.SC_OK);

        boolean consumptionExists =
                consumptionService.getPossibleConsumptions()
                        .stream()
                        .anyMatch(c -> c.getName().equals(UNUSED_CONSUMPTION_NAME));
        Assert.assertFalse(consumptionExists);
    }

    @Test
    public void removePossibleConsumption_DoesntExist(){
        removePossibleConsumption(getUnusedConsumptionId())
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    private Map<String, String> makeConsumptionRequestBody(Long ticketId, Long consumptionId){
        Map<String, String> consumptionDTO = new HashMap<>();
        consumptionDTO.put("ticketId", ticketId.toString());
        consumptionDTO.put("consumptionId", consumptionId.toString());
        return consumptionDTO;
    }

    private void invalidateTicket(){
        ticket.setValid(false);
        ticketRepository.saveAndFlush(ticket);
    }

    private Response getAllPossibleConsumptions_AsAdmin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        return getAllPossibleConsumptions(session);
    }

    private Response getAllPossibleConsumptions(SessionData session){
        return given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                get(CONSUMPTION_ENDPOINT);
    }

    private Response getTicketConsumptions(Ticket localTicket){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        return given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                get(CONSUMPTION_ENDPOINT + "/" + localTicket.getId());
    }

    private Response consumeConsumption(Ticket localTicket, Consumption localConsumption){
        return consumeConsumption(localTicket.getId(), localConsumption.getId());
    }

    private Response consumeConsumption(Long ticketId, Long consumptionId){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        Map<String, String> seatGroupDTO = makeConsumptionRequestBody(ticketId, consumptionId);

        return given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(seatGroupDTO).
                contentType(ContentType.JSON).
                post(CONSUMPTION_ENDPOINT + "/consume");
    }

    private Response consumeAndResetConsumption(Ticket localTicket, Consumption localConsumption){
        return consumeAndResetConsumption(localTicket.getId(), localConsumption.getId());
    }

    private Response consumeAndResetConsumption(Long ticketId, Long consumptionId){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        consumptionService.consume(ticketId, consumptionId);

        return resetConsumption(ticketId, consumptionId, session);
    }

    private Response resetConsumption(Long ticketId, Long consumptionId, SessionData session){
        Map<String, String> seatGroupDTO = makeConsumptionRequestBody(ticketId, consumptionId);

        return given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(seatGroupDTO).
                contentType(ContentType.JSON).
                post(CONSUMPTION_ENDPOINT + "/reset");
    }

    private Response addPossibleConsumption(String consumptionName){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        return given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(consumptionName).
                contentType(ContentType.JSON).
                post(CONSUMPTION_ENDPOINT);
    }

    private Response removePossibleConsumption(Consumption consumption){
        return removePossibleConsumption(consumption.getId());
    }


    private Response removePossibleConsumption(Long consumptionId){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        return given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(consumptionId).
                contentType(ContentType.JSON).
                delete(CONSUMPTION_ENDPOINT);
    }

    private Long getUnusedConsumptionId(){
        Long id = 0L
                ;
        while(possibleConsumptionsRepository.findById(id).isPresent()){
            id++;
        }

        return id;
    }
}
