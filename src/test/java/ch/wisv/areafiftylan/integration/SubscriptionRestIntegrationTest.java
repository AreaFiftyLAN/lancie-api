/*
 * Copyright (c) 2018  W.I.S.V. 'Christiaan Huygens'
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


import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;

import ch.wisv.areafiftylan.extras.mailupdates.model.Subscription;
import ch.wisv.areafiftylan.extras.mailupdates.model.SubscriptionDTO;
import ch.wisv.areafiftylan.extras.mailupdates.model.SubscriptionRepository;
import ch.wisv.areafiftylan.users.model.User;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

public class SubscriptionRestIntegrationTest extends XAuthIntegrationTest {

    private static final String USER_EMAIL1 = "sub1@mail.com";
    private static final String USER_EMAIL2 = "sub2@mail.com";
    private static final String USER_EMAIL3 = "sub3@mail.com";
    private static final String USER_EMAIL4 = "sub4@mail.com";
    private final String ENDPOINT = "/subscriptions/";

    private SubscriptionDTO subscriptionDTO1;

    private User user;
    private User committeMember;

    @Autowired
    private SubscriptionRepository subscriptionRepository;
    private Subscription subscription1;
    private Subscription subscription2;
    private Subscription subscription3;

    @Before
    public void setUp() throws Exception {
        subscriptionRepository.deleteAll();

        this.subscriptionDTO1 = new SubscriptionDTO();
        this.subscriptionDTO1.setEmail(USER_EMAIL1);

        this.user = createUser();
        this.committeMember = createCommitteeMember();
    }

    private void addSubscriptionsToRepository() {
        subscription1 = subscriptionRepository.save(new Subscription(USER_EMAIL1));
        subscription2 = subscriptionRepository.save(new Subscription(USER_EMAIL2));
        subscription3 = subscriptionRepository.save(new Subscription(USER_EMAIL3));
    }

    @Test
    public void testAddSubscription() throws Exception {
        given()
            .body(subscriptionDTO1)
        .when()
            .contentType(ContentType.JSON)
            .post(ENDPOINT)
        .then()
            .statusCode(HttpStatus.SC_OK);

        assertEquals(subscriptionRepository.findAll().size(), 1);
        assertEquals(subscriptionRepository.findAll().get(0).getEmail(), USER_EMAIL1);
    }

    @Test
    public void testGetAllSubscriptions() throws Exception {
        addSubscriptionsToRepository();

        given()
            .header(getXAuthTokenHeaderForUser(committeMember))
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(HttpStatus.SC_OK)
            .body("$", hasSize(Long.valueOf(subscriptionRepository.count()).intValue()))
            .body("email", hasItems(subscription1.getEmail(), subscription2.getEmail(), subscription3.getEmail()));
    }

    @Test
    public void testGetAllSubscriptionsAsNonCommittee() throws Exception {
        addSubscriptionsToRepository();

        given()
            .header(getXAuthTokenHeaderForUser(user))
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void testRemoveSubscription() throws Exception {
        addSubscriptionsToRepository();

        Long subscriptionId = subscription2.getId();
        when()
            .delete(ENDPOINT + subscriptionId)
        .then()
            .statusCode(HttpStatus.SC_OK);

        List<Subscription> expected = new LinkedList<>();
        expected.add(subscription1);
        expected.add(subscription3);
        assertEquals(expected, subscriptionRepository.findAll());
    }

    @Test
    public void testDataIntegrityViolationException() throws Exception {
        addSubscriptionsToRepository();

        given()
            .body(subscriptionDTO1)
        .when()
            .contentType(ContentType.JSON)
            .post(ENDPOINT)
        .then()
            .statusCode(HttpStatus.SC_CONFLICT);
    }

    @Test
    public void testSubscriptionNotFoundException() throws Exception {
        Long subscriptionId = 25L;
        when()
            .delete(ENDPOINT + subscriptionId)
        .then()
            .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testInvalidEmail() throws Exception {
        SubscriptionDTO invalidDTO = new SubscriptionDTO();
        invalidDTO.setEmail("abc");

        given()
            .body(invalidDTO)
        .when()
            .contentType(ContentType.JSON)
            .post(ENDPOINT)
        .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);

        List<Subscription> expected = new LinkedList<>();
        assertEquals(expected, subscriptionRepository.findAll());
    }
}
