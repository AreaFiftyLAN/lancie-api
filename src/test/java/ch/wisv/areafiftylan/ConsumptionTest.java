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
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private ConsumptionMapsRepository consumptionMapsRepository;

    @Autowired
    private PossibleConsumptionsRepository possibleConsumptionsRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ConsumptionService consumptionService;

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
                get(CONSUMPTION_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void getAllPossibleConsumptionsTestNone_Admin(){
        consumptionService.removePossibleConsumption(spicyFood.getId());
        consumptionService.removePossibleConsumption(coldMilkshake.getId());


        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                get(CONSUMPTION_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(0));
    }

    @Test
    public void getAllPossibleConsumptionsTestSingle_Admin(){
        consumptionService.removePossibleConsumption(spicyFood.getId());

        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                get(CONSUMPTION_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(1)).
                body("name", containsInAnyOrder(coldMilkshake.getName()));
    }

    @Test
    public void getAllPossibleConsumptionsTestMultiple_Admin(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                get(CONSUMPTION_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(2)).
                body("name", containsInAnyOrder(coldMilkshake.getName(), spicyFood.getName()));
    }

    @Test
    public void getTicketConsumptions_NoneConsumed(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                get(CONSUMPTION_ENDPOINT + "/" + ticket.getId()).
        then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(0));
    }

    @Test
    public void getTicketConsumptions_OneConsumed(){
        consumptionService.consume(ticket.getId(), spicyFood.getId());

        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                get(CONSUMPTION_ENDPOINT + "/" + ticket.getId()).
        then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(1)).
                body("name", containsInAnyOrder(spicyFood.getName()));
    }

    @Test
    public void getTicketConsumptions_OneConsumedAndDeleted(){
        consumptionService.consume(ticket.getId(), spicyFood.getId());
        consumptionService.removePossibleConsumption(spicyFood.getId());

        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                get(CONSUMPTION_ENDPOINT + "/" + ticket.getId()).
        then().
                statusCode(HttpStatus.SC_OK).
                body("$", hasSize(0));
    }

    @Test
    public void consumeConsumption(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(spicyFood.getId()).
                contentType(ContentType.JSON).
                post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/consume").
        then().
                statusCode(HttpStatus.SC_OK);

        ConsumptionMap ticketConsumptionMap = consumptionMapsRepository.findByTicketId(ticket.getId()).get();
        Assert.assertTrue(ticketConsumptionMap.isConsumed(spicyFood));
    }

    @Test
    public void consumeConsumption_AlreadyConsumed(){
        consumptionService.consume(ticket.getId(), spicyFood.getId());

        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(spicyFood.getId()).
                contentType(ContentType.JSON).
                post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/consume").
        then().
                statusCode(HttpStatus.SC_CONFLICT);
    }

    @Test
    public void consumeConsumption_ConsumptionDoesntExist(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(getUnusedConsumptionId()).
                contentType(ContentType.JSON).
                post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/consume").
        then().
                statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void consumeConsumption_ConsumptionDeleted(){
        consumptionService.removePossibleConsumption(spicyFood.getId());
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(spicyFood.getId()).
                contentType(ContentType.JSON).
                post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/consume").
        then().
                statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void consumeConsumption_InvalidTicket(){
        invalidateTicket();

        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(spicyFood.getId()).
                contentType(ContentType.JSON).
                post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/consume").
        then().
                statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void resetConsumption(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        consumptionService.consume(ticket.getId(), spicyFood.getId());

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(spicyFood.getId()).
                contentType(ContentType.JSON).
                post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/reset").
        then().
                statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void resetConsumption_ConsumptionDoesntExist(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        consumptionService.consume(ticket.getId(), spicyFood.getId());

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(getUnusedConsumptionId()).
                contentType(ContentType.JSON).
                post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/reset").
        then().
                statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void resetConsumption_ConsumptionDeleted(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        consumptionService.consume(ticket.getId(), spicyFood.getId());

        consumptionService.removePossibleConsumption(spicyFood.getId());

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(spicyFood.getId()).
                contentType(ContentType.JSON).
                post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/reset").
        then().
                statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void resetConsumption_InvalidTicket(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        consumptionService.consume(ticket.getId(), spicyFood.getId());

        invalidateTicket();

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(spicyFood.getId()).
                contentType(ContentType.JSON).
                post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/reset").
        then().
                statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void addPossibleConsumption(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(UNUSED_CONSUMPTION_NAME).
                contentType(ContentType.JSON).
                post(CONSUMPTION_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_OK);

        boolean consumptionExists =
                consumptionService.getPossibleConsumptions()
                        .stream()
                        .anyMatch(c -> c.getName().equals(UNUSED_CONSUMPTION_NAME));
        Assert.assertTrue(consumptionExists);
    }

    @Test
    public void addPossibleConsumption_Duplicate(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        consumptionService.addPossibleConsumption(UNUSED_CONSUMPTION_NAME);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(UNUSED_CONSUMPTION_NAME).
                contentType(ContentType.JSON).
                post(CONSUMPTION_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_CONFLICT);

    }

    @Test
    public void removePossibleConsumption(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(spicyFood.getId()).
                contentType(ContentType.JSON).
                delete(CONSUMPTION_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_OK);

        boolean consumptionExists =
                consumptionService.getPossibleConsumptions()
                        .stream()
                        .anyMatch(c -> c.getName().equals(UNUSED_CONSUMPTION_NAME));
        Assert.assertFalse(consumptionExists);
    }

    @Test
    public void removePossibleConsumption_DoesntExist(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().
                filter(sessionFilter).
                header(session.getCsrfHeader()).
        when().
                content(getUnusedConsumptionId()).
                contentType(ContentType.JSON).
                delete(CONSUMPTION_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_NOT_FOUND);
    }

    private Ticket makeTicket() {
        Ticket t = new Ticket(user, TicketType.EARLY_FULL, false, false);
        t.setValid(true);
        return ticketRepository.saveAndFlush(t);
    }

    private void invalidateTicket(){
        ticket.setValid(false);
        ticketRepository.saveAndFlush(ticket);
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
