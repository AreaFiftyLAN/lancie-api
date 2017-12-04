/*
 * Copyright (c) 2017  W.I.S.V. 'Christiaan Huygens'
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

package ch.wisv.areafiftylan.unit;

import ch.wisv.areafiftylan.extras.mailupdates.model.Subscription;
import ch.wisv.areafiftylan.extras.mailupdates.model.SubscriptionDTO;
import ch.wisv.areafiftylan.extras.mailupdates.service.SubscriptionService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.LinkedList;

import static org.junit.Assert.*;

public class SubscriptionServiceTest extends ServiceTest {

    private static final String USER_EMAIL1 = "1@mail.com";
    private static final String USER_EMAIL2 = "2@mail.com";

    @Autowired
    private SubscriptionService subscriptionService;

    private SubscriptionDTO subscriptionDTO;

    @Before
    public void setUp() {
        this.subscriptionDTO = new SubscriptionDTO();
    }

    @Test
    public void testAddSubscription() throws Exception {
        Subscription firstExpected = subscriptionService.addSubscription(USER_EMAIL1);
        assertEquals(firstExpected.getEmail(), USER_EMAIL1);
    }

    @Test
    public void testRemoveSubscription() throws Exception {
        Subscription firstExpected = subscriptionService.addSubscription(USER_EMAIL1);
        Subscription secondExpected = subscriptionService.addSubscription(USER_EMAIL2);

        subscriptionService.removeSubscription(firstExpected.getId());

        Collection<Subscription> subscriptions = subscriptionService.getSubscriptions();
        assertEquals(subscriptions.size(), 1);
        assertFalse(subscriptions.contains(firstExpected));
        assertTrue(subscriptions.contains(secondExpected));
    }

    @Test
    public void testGetSubscriptions() throws Exception {
        Subscription firstExpected = subscriptionService.addSubscription(USER_EMAIL1);
        Subscription secondExpected = subscriptionService.addSubscription(USER_EMAIL2);
        Collection<Subscription> expectedSubscriptions = new LinkedList<>();

        expectedSubscriptions.add(firstExpected);
        expectedSubscriptions.add(secondExpected);

        assertEquals(subscriptionService.getSubscriptions(), expectedSubscriptions);
    }
}