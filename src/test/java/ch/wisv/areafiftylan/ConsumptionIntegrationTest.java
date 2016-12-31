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

import ch.wisv.areafiftylan.extras.consumption.model.Consumption;
import ch.wisv.areafiftylan.extras.consumption.model.ConsumptionMap;
import ch.wisv.areafiftylan.extras.consumption.model.ConsumptionMapsRepository;
import ch.wisv.areafiftylan.extras.consumption.model.PossibleConsumptionsRepository;
import ch.wisv.areafiftylan.extras.consumption.service.ConsumptionService;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.service.repository.TicketRepository;
import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.utils.SessionData;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class ConsumptionTest extends XAuthIntegrationTest {

    @Autowired
    private ConsumptionMapsRepository consumptionMapsRepository;

    @Autowired
    private PossibleConsumptionsRepository possibleConsumptionsRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ConsumptionService consumptionService;

    private final String CONSUMPTION_ENDPOINT = "/consumptions";

    private final String UNUSED_CONSUMPTION = "Hot Chocolate";
    private final String SPICY_CONSUMPTION = "Nice Spicy Food";
    private final String MILKSHAKE_CONSUMPTION = "Deliciously Cold Milkshake";

    @Before
    public void InitConsumptionTest(){
        consumptionMapsRepository.deleteAll();
        possibleConsumptionsRepository.deleteAll();
        ticketRepository.deleteAll();
    }

    @After
    public void consumptionTestCleanup(){

    }

    @Test
    public void getAllPossibleConsumptionsTestAsUser(){
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void getAllPossibleConsumptionsTestAsAdminNone(){
        User user = createUser(true);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body(empty());
        //@formatter:on
    }

    @Test
    public void getAllPossibleConsumptionsTestAsAdminSingle(){
        User user = createUser(true);
        consumptionService.addPossibleConsumption(SPICY_CONSUMPTION);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("name", contains(SPICY_CONSUMPTION));
        //@formatter:on
    }

    @Test
    public void getAllPossibleConsumptionsTestAsAdminMultiple(){
        User user = createUser(true);
        consumptionService.addPossibleConsumption(SPICY_CONSUMPTION);
        consumptionService.addPossibleConsumption(MILKSHAKE_CONSUMPTION);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("name", containsInAnyOrder(SPICY_CONSUMPTION, MILKSHAKE_CONSUMPTION));
        //@formatter:on
    }

    @Test
    public void addAvailableConsumptionTestAsUser() {
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            post(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void addAvailableConsumptionTestAsAdmin() {
        User user = createUser(true);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(SPICY_CONSUMPTION).
        when().
            post(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("Successfully added " + SPICY_CONSUMPTION + " as a supported consumption."));
        //@formatter:on
    }

    @Test
    public void addAvailableConsumptionTestAsAdminDuplicate() {
        User user = createUser(true);
        consumptionService.addPossibleConsumption(SPICY_CONSUMPTION);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(SPICY_CONSUMPTION).
        when().
            post(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_CONFLICT).
            body("message", equalTo("Consumption " + SPICY_CONSUMPTION + " is already supported"));
        //@formatter:on
    }

    @Test
    public void removeAvailableConsumptionTestAsUser() {
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            delete(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

/*
    @Test
    public void getTicketConsumptions_NoneConsumed(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().

                header(getXAuthTokenHeaderForUser()).
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

                header(getXAuthTokenHeaderForUser()).
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

                header(getXAuthTokenHeaderForUser()).
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

                header(getXAuthTokenHeaderForUser()).
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

                header(getXAuthTokenHeaderForUser()).
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

                header(getXAuthTokenHeaderForUser()).
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

                header(getXAuthTokenHeaderForUser()).
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

                header(getXAuthTokenHeaderForUser()).
        when().
                content(spicyFood.getId()).
                contentType(ContentType.JSON).
                post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/consume").
        then().
                statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void consumeConsumption_InSuccession(){
        Ticket otherTicket = makeTicket();

        consumptionService.consume(otherTicket.getId(), spicyFood.getId());

        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().

                header(getXAuthTokenHeaderForUser()).
        when().
                content(spicyFood.getId()).
                contentType(ContentType.JSON).
                post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/consume").
        then().
                statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void resetConsumption(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        consumptionService.consume(ticket.getId(), spicyFood.getId());

        given().

                header(getXAuthTokenHeaderForUser()).
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

                header(getXAuthTokenHeaderForUser()).
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

                header(getXAuthTokenHeaderForUser()).
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

                header(getXAuthTokenHeaderForUser()).
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

                header(getXAuthTokenHeaderForUser()).
        when().
                content(UNUSED_CONSUMPTION).
                contentType(ContentType.JSON).
                post(CONSUMPTION_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_OK);

        boolean consumptionExists =
                consumptionService.getPossibleConsumptions()
                        .stream()
                        .anyMatch(c -> c.getName().equals(UNUSED_CONSUMPTION));
        Assert.assertTrue(consumptionExists);
    }

    @Test
    public void addPossibleConsumption_Duplicate(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        consumptionService.addPossibleConsumption(UNUSED_CONSUMPTION);

        given().

                header(getXAuthTokenHeaderForUser()).
        when().
                content(UNUSED_CONSUMPTION).
                contentType(ContentType.JSON).
                post(CONSUMPTION_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_CONFLICT);

    }

    @Test
    public void removePossibleConsumption(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().

                header(getXAuthTokenHeaderForUser()).
        when().
                content(spicyFood.getId()).
                contentType(ContentType.JSON).
                delete(CONSUMPTION_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_OK);

        boolean consumptionExists =
                consumptionService.getPossibleConsumptions()
                        .stream()
                        .anyMatch(c -> c.getName().equals(UNUSED_CONSUMPTION));
        Assert.assertFalse(consumptionExists);
    }

    @Test
    public void removePossibleConsumption_DoesntExist(){
        SessionData session = login(admin.getUsername(), adminCleartextPassword);

        given().

                header(getXAuthTokenHeaderForUser()).
        when().
                content(getUnusedConsumptionId()).
                contentType(ContentType.JSON).
                delete(CONSUMPTION_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_NOT_FOUND);
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
*/
