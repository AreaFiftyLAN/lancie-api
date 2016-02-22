package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Gender;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.service.OrderService;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import ch.wisv.areafiftylan.util.SessionData;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import junit.framework.Assert;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;

/**
 * Created by beer on 5-1-16.
 */
public class TicketTransferRestIntegrationTest extends IntegrationTest{
    private final String TRANSFER_ENDPOINT = "/tickettransfer";

    private User ticketReciever;
    private Ticket ticket;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private OrderService orderService;

    @Before
    public void initTransferTest(){
        ticketReciever = makeTicketReceiver();
        ticket = makeMockTicket();
    }

    @After
    public void cleanTransferTest(){
        ticketRepository.deleteAll();
        userRepository.delete(ticketReciever);
    }

    private User makeTicketReceiver(){
        User receiver = new User("receiver", new BCryptPasswordEncoder().encode("password"), "receiver@gmail.com");
        receiver.getProfile()
                .setAllFields("receiver", " of tickets", "GotYaTicket", Gender.MALE, "Money Owner 4", "2826GJ", "Tomorrowland",
                        "0906-1111", null);

        userRepository.saveAndFlush(receiver);

        return receiver;
    }

    private Ticket makeMockTicket(){
        Ticket t = new Ticket(user, TicketType.EARLY_FULL, false, false);
        t = ticketRepository.saveAndFlush(t);

        return t;
    }

    @Test
    public void testAddTransfer_Anon(){
        Map<String, String> transferRequest = new HashMap<>();
        transferRequest.put("ticketKey", ticket.getKey());
        transferRequest.put("goalUsername", ticketReciever.getUsername());

        given()
        .when()
                .content(transferRequest).contentType(ContentType.JSON)
                .post("/tickettransfer")
        .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);
        if(ticket == null) Assert.fail("Could not refresh ticket");

        Assert.assertTrue(ticket.isLockedForTransfer());
    }

    @Test
    public void testAddTransfer_Owner(){
        addTicketTransfer(user.getUsername(), "password").then().statusCode(HttpStatus.SC_OK);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);
        if(ticket == null) Assert.fail("Could not refresh ticket");

        Assert.assertTrue(ticket.isLockedForTransfer());
    }

    @Test
    public void testAddTransfer_Receiver(){
        addTicketTransfer(ticketReciever.getUsername(), "password").then().statusCode(HttpStatus.SC_FORBIDDEN);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);
        if(ticket == null) Assert.fail("Could not refresh ticket");

        Assert.assertTrue(ticket.isLockedForTransfer());
    }

    @Test
    public void testDoTransfer_Anon(){
        addTicketTransfer(user.getUsername(), "password");

        logout();

        given().
                when().
                put("/tickettransfer/" + ticket.getKey()).
                then().statusCode(HttpStatus.SC_FORBIDDEN);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);
        if(ticket == null) Assert.fail("Could not refresh ticket");

        Assert.assertTrue(ticket.isLockedForTransfer());
        Assert.assertTrue(ticket.getOwner().equals(user));
    }

    @Test
    public void testDoTransfer_Owner(){
        addTicketTransfer(user.getUsername(), "password");

        logout();

        SessionData login = login(user.getUsername(), "password");

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
                when().
                put("/tickettransfer/" + ticket.getKey()).
                then().statusCode(HttpStatus.SC_UNAUTHORIZED);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);
        if(ticket == null) Assert.fail("Could not refresh ticket");

        Assert.assertTrue(ticket.isLockedForTransfer());
        Assert.assertTrue(ticket.getOwner().equals(user));
    }


    @Test
    public void testDoTransfer_Receiver(){
        addTicketTransfer(user.getUsername(), "password");

        logout();

        SessionData login = login(ticketReciever.getUsername(), "password");

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
                when().
                put("/tickettransfer/" + ticket.getKey()).
                then().statusCode(HttpStatus.SC_OK);

        ticket = ticketRepository.findByOwnerUsername(ticketReciever.getUsername()).orElse(null);
        if(ticket == null) Assert.fail("Could not refresh ticket");

        Assert.assertFalse(ticket.isLockedForTransfer());
        Assert.assertTrue(ticket.getOwner().equals(ticketReciever));
    }

    private Response addTicketTransfer(String uname, String pw){
        Map<String, String> transferRequest = new HashMap<>();
        transferRequest.put("ticketKey", ticket.getKey());
        transferRequest.put("goalUsername", ticketReciever.getUsername());

        SessionData login = login(uname, pw);

        return given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
                when().
                content(transferRequest).contentType(ContentType.JSON).
                post("/tickettransfer")
                .then().extract().response();
    }

}
