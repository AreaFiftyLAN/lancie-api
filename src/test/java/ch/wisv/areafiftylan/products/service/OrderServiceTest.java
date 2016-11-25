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

package ch.wisv.areafiftylan.products.service;

import ch.wisv.areafiftylan.ApplicationTest;
import ch.wisv.areafiftylan.exception.OrderNotFoundException;
import ch.wisv.areafiftylan.products.model.Order;
import ch.wisv.areafiftylan.products.model.OrderStatus;
import ch.wisv.areafiftylan.users.model.Gender;
import ch.wisv.areafiftylan.users.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.spring4.SpringTemplateEngine;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationTest.class)
@ActiveProfiles("test")
@DataJpaTest
public class OrderServiceTest {

    @MockBean
    SpringTemplateEngine springTemplateEngine;
    @MockBean
    JavaMailSender javaMailSender;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TestEntityManager testEntityManager;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private User persistUser() {
        User user = new User("user@mail.com", new BCryptPasswordEncoder().encode("password"));
        user.getProfile()
                .setAllFields("first", "last", "display", LocalDate.now(), Gender.MALE, "address", "1234AB", "Delft",
                        "0612345678", null);
        return testEntityManager.persist(user);
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void getOrderById() {
        Long id = testEntityManager.persistAndGetId(new Order(), Long.class);

        Order order = orderService.getOrderById(id);

        assertEquals(id, order.getId());
    }

    @Test
    public void getOrderByIdNotFound() {
        Long id = 9999L;
        thrown.expect(OrderNotFoundException.class);
        thrown.expectMessage("Order with id: " + id + " not found");

        orderService.getOrderById(id);
    }

    @Test
    public void getOrderByIdNull() {
        thrown.expect(OrderNotFoundException.class);
        thrown.expectMessage("Order with id: ");

        orderService.getOrderById(null);
    }

    @Test
    public void findOrdersByUsername() {
        User user = persistUser();

        Order order = new Order();
        order.setUser(user);
        order = testEntityManager.persist(order);

        Collection<Order> ordersByUsername = orderService.findOrdersByUsername(user.getUsername());

        assertEquals(1, ordersByUsername.size());
    }

    @Test
    public void findOrdersByUsernameUppercase() {
        User user = persistUser();

        Order order = new Order();
        order.setUser(user);
        order = testEntityManager.persist(order);

        Collection<Order> ordersByUsername = orderService.findOrdersByUsername(user.getUsername().toUpperCase());

        assertEquals(1, ordersByUsername.size());

    }

    @Test
    public void findOrdersByUsernameMultipleOrders() {
        User user = persistUser();

        Order order = new Order();
        Order order2 = new Order();
        order.setUser(user);
        order2.setUser(user);
        testEntityManager.persist(order);
        testEntityManager.persist(order2);

        Collection<Order> ordersByUsername = orderService.findOrdersByUsername(user.getUsername());

        assertEquals(2, ordersByUsername.size());
    }

    @Test
    public void findOrdersByUsernameNull() {
        User user = persistUser();

        Order order = new Order();
        order.setUser(user);
        testEntityManager.persist(order);

        Collection<Order> ordersByUsername = orderService.findOrdersByUsername(null);

        assertEquals(0, ordersByUsername.size());
    }

    @Test
    public void getOpenOrders() {
        User user = persistUser();

        Order order = new Order();
        order.setUser(user);
        testEntityManager.persist(order);
        Order order2 = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PAID);
        testEntityManager.persist(order2);

        List<Order> openOrders = orderService.getOpenOrders(user.getUsername());

        assertEquals(1, openOrders.size());
    }

    @Test
    public void create() {

    }

    @Test
    public void addTicketToOrder() {

    }

    @Test
    public void assignOrderToUser() {

    }

    @Test
    public void removeTicketFromOrder() {

    }

    @Test
    public void requestPayment() {

    }

    @Test
    public void updateOrderStatus() {

    }

    @Test
    public void updateOrderStatus1() {

    }

    @Test
    public void adminApproveOrder() {

    }

    @Test
    public void expireOrder() {

    }

    @Test
    public void getAvailableTickets() {

    }

}