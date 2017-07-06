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


package ch.wisv.areafiftylan.integration;

import ch.wisv.areafiftylan.extras.consumption.model.Consumption;
import ch.wisv.areafiftylan.extras.consumption.model.ConsumptionMapsRepository;
import ch.wisv.areafiftylan.extras.consumption.model.PossibleConsumptionsRepository;
import ch.wisv.areafiftylan.extras.consumption.service.ConsumptionService;
import ch.wisv.areafiftylan.products.model.Ticket;
import ch.wisv.areafiftylan.products.service.repository.TicketRepository;
import ch.wisv.areafiftylan.users.model.User;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

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

    private Consumption getOrPersistConsumption(String consumption) {
        Optional<Consumption> consumptionOptional = consumptionsRepository.findByName(consumption);

        return consumptionOptional.orElseGet(() -> consumptionService.addPossibleConsumption(consumption));
    }

    @Test
    public void getAllPossibleConsumptionsTestAsUser() {
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
    public void getAllPossibleConsumptionsTestAsAdmin() {
        User user = createAdmin();
        getOrPersistConsumption(CONSUMPTION);

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
    public void getAllPossibleConsumptionsTestAsAdminMultiple() {
        User user = createAdmin();
        getOrPersistConsumption(CONSUMPTION);
        String MILKSHAKE_CONSUMPTION = "Deliciously Cold Milkshake";
        getOrPersistConsumption(MILKSHAKE_CONSUMPTION);

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
            body("addAvailableConsumptionTestAsUser").
        when().
            post(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void addAvailableConsumptionTestAsAdmin() {
        User user = createAdmin();
        String consumption = "addAvailableConsumptionTestAsAdmin";

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption).
        when().
            post(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("Successfully added " + consumption + " as a supported consumption."));
        //@formatter:on
    }

    @Test
    public void addAvailableConsumptionTestAsAdminDuplicate() {
        User user = createAdmin();
        getOrPersistConsumption(CONSUMPTION);

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
        Consumption consumption = getOrPersistConsumption(CONSUMPTION);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            body(consumption.getId()).
            contentType(ContentType.JSON).
            delete(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void removeAvailableConsumptionTestAsAdmin() {
        User user = createAdmin();
        Consumption consumption = getOrPersistConsumption(CONSUMPTION);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
            contentType(ContentType.JSON).
        when().
            delete(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("Successfully removed " + CONSUMPTION + " as a supported consumption."));
        //@formatter:on
    }

    @Test
    public void removeAvailableConsumptionTestAsAdminNoneThere() {
        User user = createAdmin();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(9999F).
            contentType(ContentType.JSON).
        when().
            delete(CONSUMPTION_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_NOT_FOUND).
            body("message", equalTo("Can't find a consumption with id: 9999"));
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
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);
        Consumption consumption = getOrPersistConsumption(CONSUMPTION);
        consumptionService.consume(ticket.getId(), consumption.getId());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(CONSUMPTION_ENDPOINT + "/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("name", contains(CONSUMPTION));
        //@formatter:on
    }

    @Test
    public void consumptionsMadeTestAsAdminNoConsumptions() {
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(CONSUMPTION_ENDPOINT + "/" + ticket.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("", empty());
        //@formatter:on
    }

    @Test
    public void consumptionsMadeTestAsAdminInvalidTicket() {
        User user = createAdmin();
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
        Consumption consumption = getOrPersistConsumption(CONSUMPTION);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
            contentType(ContentType.JSON).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/consume").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void consumeTestAsAdmin() {
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);
        Consumption consumption = getOrPersistConsumption(CONSUMPTION);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
            contentType(ContentType.JSON).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/consume").
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("Successfully consumed consumption"));
        //@formatter:on
    }

    @Test
    public void consumeTestAsAdminAlreadyConsumed() {
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);
        Consumption consumption = getOrPersistConsumption(CONSUMPTION);
        consumptionService.consume(ticket.getId(), consumption.getId());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
            contentType(ContentType.JSON).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/consume").
        then().
            statusCode(HttpStatus.SC_CONFLICT).
            body("message", equalTo("Consumption " + consumption.getName() + " has already been consumed."));
        //@formatter:on
    }

    @Test
    public void consumeTestAsAdminDoesntExist() {
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(9999F).
            contentType(ContentType.JSON).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/consume").
        then().
            statusCode(HttpStatus.SC_NOT_FOUND);
        //@formatter:on
    }

    @Test
    public void consumeTestAsAdminInvalidTicket() {
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);
        ticket.setValid(false);
        ticketRepository.saveAndFlush(ticket);
        Consumption consumption = getOrPersistConsumption(CONSUMPTION);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
            contentType(ContentType.JSON).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/consume").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST).
            body("message", equalTo("Ticket is invalid; It can not be used for consumptions."));
        //@formatter:on
    }

    @Test
    public void consumeTestAsAdminConsumedByOther() {
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);
        Consumption consumption = getOrPersistConsumption(CONSUMPTION);
        consumptionService.consume(createTicketForUser(createUser()).getId(), consumption.getId());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
            contentType(ContentType.JSON).
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
        Consumption consumption = getOrPersistConsumption(CONSUMPTION);
        consumptionService.consume(ticket.getId(), consumption.getId());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
            contentType(ContentType.JSON).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/reset").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void resetTestAsAdmin() {
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);
        Consumption consumption = getOrPersistConsumption(CONSUMPTION);
        consumptionService.consume(ticket.getId(), consumption.getId());

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
            contentType(ContentType.JSON).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/reset").
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("Successfully reset consumption"));
        //@formatter:on
    }

    @Test
    public void resetTestAsAdminDoesntExist() {
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(9999F).
            contentType(ContentType.JSON).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/reset").
        then().
            statusCode(HttpStatus.SC_NOT_FOUND).
            body("message", equalTo("Can't find a consumption with id: 9999"));
        //@formatter:on
    }

    @Test
    public void resetTestAsAdminInvalidTicket() {
        User user = createAdmin();
        Ticket ticket = createTicketForUser(user);
        ticket.setValid(false);
        ticketRepository.saveAndFlush(ticket);
        Consumption consumption = getOrPersistConsumption(CONSUMPTION);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(consumption.getId()).
            contentType(ContentType.JSON).
        when().
            post(CONSUMPTION_ENDPOINT + "/" + ticket.getId() + "/reset").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST).
            body("message", equalTo("Ticket is invalid; It can not be used for consumptions."));
        //@formatter:on
    }
}
