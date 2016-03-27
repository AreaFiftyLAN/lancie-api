package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.Event;
import ch.wisv.areafiftylan.service.repository.EventRepository;
import ch.wisv.areafiftylan.util.SessionData;
import com.jayway.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;


/**
 * Created by Sille Kamoen on 27-3-16.
 */
public class EventRestIntegrationTest extends IntegrationTest {

    private final String EVENTS_ENDPOINT = "/events";
    protected Event testEvent;

    @Autowired
    EventRepository eventRepository;

    @Before
    public void setupEventTests() {
        testEvent = eventRepository.save(new Event("Test Event", 2, 5));
    }

    @After
    public void cleanupEventTests() {
        eventRepository.deleteAll();
    }

    //region Test Add Events

    @Test
    public void testAddEventAsAnon() {
        Map<String, String> eventDTO = new HashMap<>();
        eventDTO.put("name", "event");
        eventDTO.put("teamLimit", "10");
        eventDTO.put("teamSize", "2");
        //@formatter:off
        given().
        when().
            content(eventDTO).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAddEventAsUser() {
        Map<String, String> eventDTO = new HashMap<>();
        eventDTO.put("name", "event");
        eventDTO.put("teamLimit", "10");
        eventDTO.put("teamSize", "2");

        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(eventDTO).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAddEventAsAdmin() {
        Map<String, String> eventDTO = new HashMap<>();
        eventDTO.put("name", "event");
        eventDTO.put("teamLimit", "10");
        eventDTO.put("teamSize", "2");

        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(eventDTO).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_CREATED).
            header("location", containsString("/events/")).
            body("object.name", is("event"));
        //@formatter:on
    }

    @Test
    public void testAddEventDuplicateNameAsAdmin() {

    }

    @Test
    public void testAddEventMissingNameAsAdmin() {

    }

    @Test
    public void testAddEventMissingTeamSizeAsAdmin() {

    }

    @Test
    public void testAddEventMissingTeamLimit() {

    }

    @Test
    public void testAddEventEmptyBody() {

    }

    //endregion

    //region Test Change Events

    @Test
    public void testChangeEventAsAnon() {

    }

    @Test
    public void testChangeEventAsUser() {

    }

    @Test
    public void testChangeEventAsAdmin() {

    }

    @Test
    public void testChangeEventNameOnly() {

    }

    @Test
    public void testChangeEventLimitOnly() {

    }

    @Test
    public void testChangeEventTeamSizeOnly() {

    }

    @Test
    public void testChangeEventNonExistingId() {

    }
    //endregion

    //region Test Delete Events
    @Test
    public void testDeleteEventAsAnon() {

    }

    @Test
    public void testDeleteEventAsUser() {

    }

    @Test
    public void testDeleteEventAsAdmin() {

    }

    @Test
    public void testDeleteWithoutEventId() {

    }

    @Test
    public void testDeleteNonExistingId() {

    }
    //endregion

    //region Test Register Teams
    @Test
    public void testRegisterTeamAsAnon() {

    }

    @Test
    public void testRegisterTeamAsUser() {

    }

    @Test
    public void testRegisterAsTeamMember() {

    }

    @Test
    public void testRegisterAsTeamCaptain() {

    }

    @Test
    public void testRegisterAsAdmin() {

    }

    @Test
    public void testRegisterForClosedEvent() {

    }

    @Test
    public void testRegisterWrongTeamSize() {

    }

    @Test
    public void testRegisterTeamLimitReached() {

    }

    @Test
    public void testRegisterTeamNonExistingTeamId() {

    }

    @Test
    public void testRegisterTeamNonExistingEventId() {

    }

    @Test
    public void testRegisterTeamAlreadyRegistered() {

    }

    @Test
    public void testRegisterTeamWithMemberAlreadyRegistered() {

    }

    @Test
    public void testRegisterTeamWithCaptainAlreadyRegistered() {

    }
    //endregion

    //region Test Get Events
    @Test
    public void testGetAllEvents() {

    }

    @Test
    public void testGetTeamsForEvent() {

    }
    //endregion
}
