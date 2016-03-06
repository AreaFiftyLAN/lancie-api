package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Gender;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import ch.wisv.areafiftylan.util.SessionData;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.junit.Assert;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by beer on 5-1-16.
 */
public class TicketTransferRestIntegrationTest extends IntegrationTest{
    private final String TRANSFER_ENDPOINT = "/tickets/transfer";

    private User outsider;
    private User ticketReceiver;
    private Ticket ticket;

    @Autowired
    private TicketRepository ticketRepository;

    @Before
    public void initTransferTest(){
        outsider = makeOutsider();
        ticketReceiver = makeTicketReceiver();
        ticket = makeTicket();
    }

    @After
    public void cleanTransferTest(){
        ticketRepository.deleteAll();
        userRepository.delete(ticketReceiver);
    }

    private User makeTicketReceiver(){
        User receiver = new User("receiver", new BCryptPasswordEncoder().encode("password"), "receiver@gmail.com");
        receiver.getProfile()
                .setAllFields("receiver", " of tickets", "GotYaTicket", Gender.MALE, "Money Owner 4", "2826GJ", "Tomorrowland",
                        "0906-1111", null);

        userRepository.saveAndFlush(receiver);

        return receiver;
    }

    private User makeOutsider(){
        User outsider = new User("outsider", new BCryptPasswordEncoder().encode("password"), "outsider@gmail.com");
        outsider.getProfile()
                .setAllFields("Nottin", "Todoeo Witit", "Lookinin", Gender.FEMALE, "LoserStreet 1", "2826GJ", "China",
                        "0906-3928", null);

        userRepository.saveAndFlush(outsider);

        return outsider;
    }

    private Ticket makeTicket(){
        Ticket t = new Ticket(user, TicketType.EARLY_FULL, false, false);
        t = ticketRepository.saveAndFlush(t);

        return t;
    }

    @Test
    public void testAddTransfer_Anon(){
        Map<String, String> transferRequest = new HashMap<>();
        transferRequest.put("goalUsername", ticketReceiver.getUsername());

        given().
        when().
                content(transferRequest + "/" + ticket.getKey()).contentType(ContentType.JSON).
                post(TRANSFER_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);

        Assert.assertTrue(!ticket.isTransferrable());
        Assert.assertTrue(ticket.getOwner().equals(user));
        Assert.assertTrue(ticket.getTransferGoalOwner() == null);
    }

    @Test
    public void testAddTransfer_Owner(){
        addTicketTransfer(user.getUsername(), "password").then().statusCode(HttpStatus.SC_OK);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);

        Assert.assertTrue(ticket.isTransferrable());
        Assert.assertTrue(ticket.getOwner().equals(user));
        Assert.assertTrue(ticket.getTransferGoalOwner().equals(ticketReceiver));
    }

    @Test
    public void testAddTransfer_Receiver(){
        addTicketTransfer(ticketReceiver.getUsername(), "password").then().statusCode(HttpStatus.SC_FORBIDDEN);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);

        Assert.assertTrue(!ticket.isTransferrable());
        Assert.assertTrue(ticket.getOwner().equals(user));
        Assert.assertTrue(ticket.getTransferGoalOwner() == null);
    }

    @Test
    public void testAddTransfer_Outsider(){
        addTicketTransfer(outsider.getUsername(), "password").then().statusCode(HttpStatus.SC_FORBIDDEN);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);

        Assert.assertTrue(!ticket.isTransferrable());
        Assert.assertTrue(ticket.getOwner().equals(user));
        Assert.assertTrue(ticket.getTransferGoalOwner() == null);
    }

    @Test
    public void testDoTransfer_Anon(){
        addTicketTransfer(user.getUsername(), "password");

        given().
        when().
                put(TRANSFER_ENDPOINT + "/" + ticket.getKey()).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);

        Assert.assertTrue(ticket.isTransferrable());
        Assert.assertTrue(ticket.getOwner().equals(user));
        Assert.assertTrue(ticket.getTransferGoalOwner().equals(ticketReceiver));
    }

    @Test
    public void testDoTransfer_Owner(){
        addTicketTransfer(user.getUsername(), "password");

        SessionData login = login(user.getUsername(), "password");

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
        when().
                put(TRANSFER_ENDPOINT + "/" + ticket.getKey()).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);

        Assert.assertTrue(ticket.isTransferrable());
        Assert.assertTrue(ticket.getOwner().equals(user));
        Assert.assertTrue(ticket.getTransferGoalOwner().equals(ticketReceiver));
    }


    @Test
    public void testDoTransfer_Receiver(){
        addTicketTransfer(user.getUsername(), "password");

        SessionData login = login(ticketReceiver.getUsername(), "password");

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
        when().
                put(TRANSFER_ENDPOINT + "/" + ticket.getKey()).
        then().
                statusCode(HttpStatus.SC_OK);

        ticket = ticketRepository.findByOwnerUsername(ticketReceiver.getUsername()).orElse(null);

        Assert.assertTrue(!ticket.isTransferrable());
        Assert.assertTrue(ticket.getOwner().equals(ticketReceiver));
        Assert.assertTrue(ticket.getTransferGoalOwner() == null);
    }

    @Test
    public void testDoTransfer_Outsider(){
        addTicketTransfer(user.getUsername(), "password");

        SessionData login = login(outsider.getUsername(), "password");

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
        when().
                put(TRANSFER_ENDPOINT + "/" + ticket.getKey()).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);

        Assert.assertTrue(ticket.isTransferrable());
        Assert.assertTrue(ticket.getOwner().equals(user));
        Assert.assertTrue(ticket.getTransferGoalOwner().equals(ticketReceiver));
    }

    @Test
    public void testCancelTransferNotTransferrable_Anon(){
        given().
        when().
                delete(TRANSFER_ENDPOINT + "/" + ticket.getKey()).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);

        Assert.assertTrue(!ticket.isTransferrable());
        Assert.assertTrue(ticket.getOwner().equals(user));
        Assert.assertTrue(ticket.getTransferGoalOwner() == null);
    }

    @Test
    public void testCancelTransfer_Anon(){
        addTicketTransfer(user.getUsername(), "password");

        given().
        when().
                delete(TRANSFER_ENDPOINT + "/" + ticket.getKey()).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);

        Assert.assertTrue(ticket.isTransferrable());
        Assert.assertTrue(ticket.getOwner().equals(user));
        Assert.assertTrue(ticket.getTransferGoalOwner().equals(ticketReceiver));
    }

    @Test
    public void testCancelTransferNotTransferrable_Owner(){
        SessionData login = login(user.getUsername(), "password");

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
        when().
                delete(TRANSFER_ENDPOINT + "/" + ticket.getKey()).
        then().
                statusCode(HttpStatus.SC_BAD_REQUEST);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);

        Assert.assertTrue(!ticket.isTransferrable());
        Assert.assertTrue(ticket.getOwner().equals(user));
        Assert.assertTrue(ticket.getTransferGoalOwner() == null);
    }

    @Test
    public void testCancelTransfer_Owner(){
        addTicketTransfer(user.getUsername(), "password");

        SessionData login = login(user.getUsername(), "password");

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
        when().
                delete(TRANSFER_ENDPOINT + "/" + ticket.getKey()).
        then().
                statusCode(HttpStatus.SC_OK);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);

        Assert.assertTrue(!ticket.isTransferrable());
        Assert.assertTrue(ticket.getOwner().equals(user));
        Assert.assertTrue(ticket.getTransferGoalOwner() == null);

    }

    @Test
    public void testCancelTransferNotTransferrable_Receiver(){
        SessionData login = login(ticketReceiver.getUsername(), "password");

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
        when().
                delete(TRANSFER_ENDPOINT + "/" + ticket.getKey()).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);

        Assert.assertTrue(!ticket.isTransferrable());
        Assert.assertTrue(ticket.getOwner().equals(user));
        Assert.assertTrue(ticket.getTransferGoalOwner() == null);
    }

    @Test
    public void testCancelTransfer_Receiver(){
        addTicketTransfer(user.getUsername(), "password");

        SessionData login = login(ticketReceiver.getUsername(), "password");

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
        when().
                delete(TRANSFER_ENDPOINT + "/" + ticket.getKey()).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);

        Assert.assertTrue(ticket.isTransferrable());
        Assert.assertTrue(ticket.getOwner().equals(user));
        Assert.assertTrue(ticket.getTransferGoalOwner().equals(ticketReceiver));
    }

    @Test
    public void testCancelTransferNotTransferrable_OutsideUser(){
        SessionData login = login(outsider.getUsername(), "password");

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
        when().
                delete(TRANSFER_ENDPOINT + "/" + ticket.getKey()).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);

        Assert.assertTrue(!ticket.isTransferrable());
        Assert.assertTrue(ticket.getOwner().equals(user));
        Assert.assertTrue(ticket.getTransferGoalOwner() == null);
    }

    @Test
    public void testCancelTransfer_OutsideUser(){
        addTicketTransfer(user.getUsername(), "password");

        SessionData login = login(outsider.getUsername(), "password");

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
        when().
                delete(TRANSFER_ENDPOINT + "/" + ticket.getKey()).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        ticket = ticketRepository.findByKey(ticket.getKey()).orElse(null);

        Assert.assertTrue(ticket.isTransferrable());
        Assert.assertTrue(ticket.getOwner().equals(user));
        Assert.assertTrue(ticket.getTransferGoalOwner().equals(ticketReceiver));

    }

    private Response addTicketTransfer(String uname, String pw){
        Map<String, String> transferRequest = new HashMap<>();
        transferRequest.put("goalUsername", ticketReceiver.getUsername());

        SessionData login = login(uname, pw);

        Response r = given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
        when().
                content(transferRequest).contentType(ContentType.JSON).
                post(TRANSFER_ENDPOINT + "/" + ticket.getKey()).
        then().
                extract().response();

        logout();

        return r;
    }

}
