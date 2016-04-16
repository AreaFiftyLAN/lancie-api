package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.exception.TokenNotFoundException;
import ch.wisv.areafiftylan.model.Ticket;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.model.util.Gender;
import ch.wisv.areafiftylan.model.util.TicketType;
import ch.wisv.areafiftylan.security.token.TicketTransferToken;
import ch.wisv.areafiftylan.service.repository.TicketRepository;
import ch.wisv.areafiftylan.service.repository.token.TicketTransferTokenRepository;
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

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by beer on 5-1-16.
 */
public class TicketTransferRestIntegrationTest extends IntegrationTest{
    private final String TRANSFER_ENDPOINT = "/tickets/transfer";

    private User ticketReceiver;
    private final String ticketReceiverCleartextPassword = "password";
    private Ticket ticket;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketTransferTokenRepository tttRepository;

    @Before
    public void initTransferTest(){
        ticketReceiver = makeTicketReceiver();
        ticket = makeTicket();
    }

    @After
    public void cleanTransferTest(){
        tttRepository.deleteAll();
        ticketRepository.deleteAll();
    }

    private User makeTicketReceiver(){
        User receiver = new User("receiver", new BCryptPasswordEncoder().encode(ticketReceiverCleartextPassword), "receiver@gmail.com");
        receiver.getProfile()
                .setAllFields("receiver", " of tickets", "GotYaTicket", Gender.MALE, "Money Owner 4", "2826GJ", "Tomorrowland",
                        "0906-1111", null);

        return userRepository.saveAndFlush(receiver);
    }

    private Ticket makeTicket(){
        Ticket t = new Ticket(user, TicketType.EARLY_FULL, false, false);
        t = ticketRepository.saveAndFlush(t);

        return t;
    }

    @Test
    public void testAddTransfer_Anon(){
        given().
        when().
                content(ticketReceiver.getUsername()).contentType(ContentType.TEXT).
                post(TRANSFER_ENDPOINT + "/" + ticket.getId()).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        Assert.assertTrue(tttRepository.count() == 0);
    }

    @Test
    public void testAddTransfer_Owner(){
        String token = addTicketTransfer(user.getUsername(), userCleartextPassword)
                .then()
                    .statusCode(HttpStatus.SC_OK)
                .extract().path("object.token");

        TicketTransferToken ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ttt.getTicket().getOwner().equals(user));
        Assert.assertTrue(ttt.getUser().equals(ticketReceiver));
    }

    @Test
    public void testAddTransfer_Receiver(){
        addTicketTransfer(ticketReceiver.getUsername(), ticketReceiverCleartextPassword)
                .then()
                    .statusCode(HttpStatus.SC_FORBIDDEN);

        Assert.assertTrue(tttRepository.count() == 0);
    }

    @Test
    public void testAddTransfer_Outsider(){
        addTicketTransfer(outsider.getUsername(), outsiderCleartextPassword)
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        Assert.assertTrue(tttRepository.count() == 0);
    }

    @Test
    public void testDoTransfer_Anon(){
        TicketTransferToken ttt = addTicketTransferGetToken();

        given().
        when().
                content(ttt.getToken()).
                put(TRANSFER_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        String token = ttt.getToken();
        ticket = ticketRepository.findOne(ticket.getId());
        ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(user));
    }

    @Test
    public void testDoTransfer_Owner(){
        TicketTransferToken ttt = addTicketTransferGetToken();

        SessionData login = login(user.getUsername(), userCleartextPassword);

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
        when().
                content(ttt.getToken()).
                put(TRANSFER_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        String token = ttt.getToken();
        ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(user));
    }


    @Test
    public void testDoTransfer_Receiver(){
        TicketTransferToken ttt = addTicketTransferGetToken();

        SessionData login = login(ticketReceiver.getUsername(), ticketReceiverCleartextPassword);

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
        when().
                content(ttt.getToken()).
                put(TRANSFER_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_OK);

        String token = ttt.getToken();
        ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(!ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(ticketReceiver));
    }

    @Test
    public void testDoTransfer_Outsider(){
        TicketTransferToken ttt = addTicketTransferGetToken();

        SessionData login = login(outsider.getUsername(), outsiderCleartextPassword);

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
        when().
                content(ttt.getToken()).
                put(TRANSFER_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        String token = ttt.getToken();
        ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(user));
    }

    @Test
    public void testCancelTransfer_Anon(){
        TicketTransferToken ttt = addTicketTransferGetToken();

        given().
        when().
                content(ttt.getToken()).
                delete(TRANSFER_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        String token = ttt.getToken();
        ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(user));
    }

    @Test
    public void testCancelTransfer_Owner(){
        TicketTransferToken ttt = addTicketTransferGetToken();

        SessionData login = login(user.getUsername(), userCleartextPassword);

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
        when().
                content(ttt.getToken()).
                delete(TRANSFER_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_OK);

        String token = ttt.getToken();
        ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(!ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(user));
    }

    @Test
    public void testCancelTransfer_Receiver(){
        TicketTransferToken ttt = addTicketTransferGetToken();

        SessionData login = login(ticketReceiver.getUsername(), ticketReceiverCleartextPassword);

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
        when().
                content(ttt.getToken()).
                delete(TRANSFER_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        String token = ttt.getToken();
        ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(user));
    }

    @Test
    public void testCancelTransfer_OutsideUser(){
        TicketTransferToken ttt = addTicketTransferGetToken();

        SessionData login = login(outsider.getUsername(), outsiderCleartextPassword);

        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
        when().
                content(ttt.getToken()).
                delete(TRANSFER_ENDPOINT).
        then().
                statusCode(HttpStatus.SC_FORBIDDEN);

        String token = ttt.getToken();
        ttt = tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
        ticket = ticketRepository.findOne(ticket.getId());

        Assert.assertTrue(ttt.isValid());
        Assert.assertTrue(ticket.getOwner().equals(user));
    }

    private Response addTicketTransfer(String uname, String pw){
        SessionData login = login(uname, pw);

        Response r = given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
        when().
                content(ticketReceiver.getUsername()).contentType(ContentType.TEXT).
                post(TRANSFER_ENDPOINT + "/" + ticket.getId()).
        then().
                extract().response();

        logout();

        return r;
    }

    private TicketTransferToken addTicketTransferGetToken(){
        String token = addTicketTransfer(user.getUsername(), userCleartextPassword).
            then().
                extract().path("object.token");

        return tttRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(token));
    }
}
