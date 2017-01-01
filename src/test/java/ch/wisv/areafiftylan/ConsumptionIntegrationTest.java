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
import ch.wisv.areafiftylan.extras.consumption.model.ConsumptionMapsRepository;
import ch.wisv.areafiftylan.extras.consumption.model.PossibleConsumptionsRepository;
import ch.wisv.areafiftylan.extras.consumption.service.ConsumptionService;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.service.repository.TicketRepository;
import ch.wisv.areafiftylan.users.model.User;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ConsumptionIntegrationTest extends XAuthIntegrationTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PossibleConsumptionsRepository consumptionsRepository;

    @Autowired
    private ConsumptionMapsRepository consumptionMapsRepository;

    @Autowired
    private ConsumptionService consumptionService;

    private final String CONSUMPTION_ENDPOINT = "/consumptions";

    private final String CONSUMPTION = "Delicious Consumption";

    @Before
    public void InitConsumptionTest(){
        // Needs to happen before clearing the other two, otherwise shit hits the fan.
        consumptionMapsRepository.deleteAll();
        consumptionsRepository.deleteAll();
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
            body("", hasSize(0));
        //@formatter:on
    }

    @Test
    public void getAllPossibleConsumptionsTestAsAdminSingle(){
        User user = createUser(true);
        consumptionService.addPossibleConsumption(CONSUMPTION);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("name", contains(CONSUMPTION));
        //@formatter:on
    }

    @Test
    public void getAllPossibleConsumptionsTestAsAdminMultiple(){
        User user = createUser(true);
        consumptionService.addPossibleConsumption(CONSUMPTION);
        String MILKSHAKE_CONSUMPTION = "Deliciously Cold Milkshake";
        consumptionService.addPossibleConsumption(MILKSHAKE_CONSUMPTION);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("name", containsInAnyOrder(CONSUMPTION, MILKSHAKE_CONSUMPTION));
        //@formatter:on
    }

    @Test
    public void addAvailableConsumptionTestAsUser() {
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(CONSUMPTION).
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
            body(CONSUMPTION).
        when().
            post(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("Successfully added " + CONSUMPTION + " as a supported consumption."));
        //@formatter:on
    }

    @Test
    public void addAvailableConsumptionTestAsAdminDuplicate() {
        User user = createUser(true);
        consumptionService.addPossibleConsumption(CONSUMPTION);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(CONSUMPTION).
        when().
            post(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_CONFLICT).
            body("message", equalTo("Consumption " + CONSUMPTION + " is already supported"));
        //@formatter:on
    }

    @Test
    public void removeAvailableConsumptionTestAsUser() {
        User user = createUser();
        Consumption consumption = consumptionService.addPossibleConsumption(CONSUMPTION);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
        when().
            delete(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void removeAvailableConsumptionTestAsAdmin() {
        User user = createUser(true);
        Consumption consumption = consumptionService.addPossibleConsumption(CONSUMPTION);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
        when().
            delete(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("Successfully removed " + CONSUMPTION + " as a supported consumption."));
        //@formatter:on
    }

    @Test
    public void removeAvailableConsumptionTestAsAdminNoneThere() {
        User user = createUser(true);
        Consumption consumption = consumptionService.addPossibleConsumption(CONSUMPTION);
        consumption = consumptionService.removePossibleConsumption(consumption.getId());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
        when().
            delete(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("Successfully removed " + CONSUMPTION + " as a supported consumption."));
        //@formatter:on
    }

    @Test
    public void consumptionsMadeTestAsUser() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(CONSUMPTION_ENDPOINT + "/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void consumptionsMadeTestAsAdmin() {
        User user = createUser(true);
        Ticket ticket = createTicketForUser(user);
        Consumption consumption = consumptionService.addPossibleConsumption(CONSUMPTION);
        consumptionService.consume(ticket.getId(), consumption.getId());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(CONSUMPTION_ENDPOINT + "/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object", equalTo(Collections.singletonList(consumption)));
        //@formatter:on
    }

    @Test
    public void consumptionsMadeTestAsAdminNoConsumptions() {
        User user = createUser(true);
        Ticket ticket = createTicketForUser(user);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(CONSUMPTION_ENDPOINT + "/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object", equalTo(Collections.emptyList()));
        //@formatter:on
    }

    @Test
    public void consumptionsMadeTestAsAdminInvalidTicket() {
        User user = createUser(true);
        Ticket ticket = createTicketForUser(user);
        ticket.setValid(false);
        ticketRepository.saveAndFlush(ticket);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(CONSUMPTION_ENDPOINT + "/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST).
            body("message", equalTo("Ticket is invalid; It can not be used for consumptions."));
        //@formatter:on
    }

    @Test
    public void consumeTestAsUser() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        Consumption consumption = consumptionService.addPossibleConsumption(CONSUMPTION);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/consume").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void consumeTestAsAdmin() {
        User user = createUser(true);
        Ticket ticket = createTicketForUser(user);
        Consumption consumption = consumptionService.addPossibleConsumption(CONSUMPTION);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/consume").
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("Successfully consumed consumption"));
        //@formatter:on
    }

    @Test
    public void consumeTestAsAdminAlreadyConsumed() {
        User user = createUser(true);
        Ticket ticket = createTicketForUser(user);
        Consumption consumption = consumptionService.addPossibleConsumption(CONSUMPTION);
        consumptionService.consume(ticket.getId(), consumption.getId());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/consume").
        then().
            statusCode(HttpStatus.SC_CONFLICT).
            body("message", equalTo("Consumption " + consumption.getName() + " has already been consumed."));
        //@formatter:on
    }

    @Test
    public void consumeTestAsAdminDoesntExist() {
        User user = createUser(true);
        Ticket ticket = createTicketForUser(user);
        Consumption consumption = consumptionService.addPossibleConsumption(CONSUMPTION);
        consumption = consumptionService.removePossibleConsumption(consumption.getId());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/consume").
        then().
            statusCode(HttpStatus.SC_NOT_FOUND);
        //@formatter:on
    }

    @Test
    public void consumeTestAsAdminInvalidTicket() {
        User user = createUser(true);
        Ticket ticket = createTicketForUser(user);
        ticket.setValid(false);
        ticketRepository.saveAndFlush(ticket);
        Consumption consumption = consumptionService.addPossibleConsumption(CONSUMPTION);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/consume").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST).
            body("message", equalTo("Ticket is invalid; It can not be used for consumptions."));
        //@formatter:on
    }

    @Test
    public void consumeTestAsAdminConsumedByOther() {
        User user = createUser(true);
        Ticket ticket = createTicketForUser(user);
        Consumption consumption = consumptionService.addPossibleConsumption(CONSUMPTION);
        consumptionService.consume(createTicketForUser(createUser()).getId(), consumption.getId());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/consume").
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("Successfully consumed consumption"));
        //@formatter:on
    }

    @Test
    public void resetTestAsUser() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        Consumption consumption = consumptionService.addPossibleConsumption(CONSUMPTION);
        consumptionService.consume(ticket.getId(), consumption.getId());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/reset").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void resetTestAsAdmin() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        Consumption consumption = consumptionService.addPossibleConsumption(CONSUMPTION);
        consumptionService.consume(ticket.getId(), consumption.getId());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/reset").
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("Successfully reset consumption"));
        //@formatter:on
    }

    @Test
    public void resetTestAsAdminDoesntExist() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        Consumption consumption = consumptionService.addPossibleConsumption(CONSUMPTION);
        consumption = consumptionService.removePossibleConsumption(consumption.getId());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/reset").
        then().
            statusCode(HttpStatus.SC_NOT_FOUND).
            body("message", equalTo("Successfully reset consumption"));
        //@formatter:on
    }

    @Test
    public void resetTestAsAdminInvalidTicket() {
        User user = createUser();
        Ticket ticket = createTicketForUser(user);
        ticket.setValid(false);
        ticketRepository.saveAndFlush(ticket);
        Consumption consumption = consumptionService.addPossibleConsumption(CONSUMPTION);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/reset").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST).
            body("message", equalTo("Ticket is invalid; It can not be used for consumptions."));
        //@formatter:on
    }
}
