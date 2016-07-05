package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.model.Event;
import ch.wisv.areafiftylan.model.Team;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.repository.EventRepository;
import ch.wisv.areafiftylan.service.repository.TeamRepository;
import ch.wisv.areafiftylan.service.repository.UserRepository;
import ch.wisv.areafiftylan.util.SessionData;
import com.jayway.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


/**
 * Created by Sille Kamoen on 27-3-16.
 */
public class EventRestIntegrationTest extends IntegrationTest {

    private final String EVENTS_ENDPOINT = "/events";
    protected Event testEvent;

    private Team team1;
    private Team team2;

    private User teamCaptain;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    UserRepository userRepository;

    @Before
    public void setupEventTests() {
        testEvent = new Event("Test Event", 2, 5);
        testEvent.setOpenForRegistration(true);
        testEvent = eventRepository.save(testEvent);

        teamCaptain = userRepository
                .save(new User("captain", new BCryptPasswordEncoder().encode("password"), "captain@team.com"));

        team1 = teamRepository.save(new Team("team1", teamCaptain));
        team2 = new Team("team2", teamCaptain);
        team2.addMember(user);
        team2 = teamRepository.save(team2);
    }

    @After
    public void cleanupEventTests() {
        eventRepository.deleteAll();
        teamRepository.deleteAll();
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
            body("object.eventName", is("event"));
        //@formatter:on
    }

    private void addEvent(String username, String name, String teamLimit, String teamSize) {
        Map<String, String> eventDTO = new HashMap<>();
        eventDTO.put("name", name);
        eventDTO.put("teamLimit", teamLimit);
        eventDTO.put("teamSize", teamSize);

        SessionData login = login(username);

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
            body("object.eventName", is("event"));
        //@formatter:on
    }

    @Test
    public void testAddEventDuplicateNameAsAdmin() {
        Map<String, String> eventDTO = new HashMap<>();
        eventDTO.put("name", "event");
        eventDTO.put("teamLimit", "10");
        eventDTO.put("teamSize", "2");

        addEvent("admin", eventDTO.get("name"), eventDTO.get("teamLimit"), eventDTO.get("teamSize"));

        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(eventDTO).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_CONFLICT);
        //@formatter:on

        Assert.assertEquals(2, eventRepository.count());
    }

    @Test
    public void testAddEventMissingNameAsAdmin() {
        Map<String, String> eventDTO = new HashMap<>();
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
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on

        Assert.assertEquals(1, eventRepository.count());
    }

    @Test
    public void testAddEventMissingTeamSizeAsAdmin() {
        Map<String, String> eventDTO = new HashMap<>();
        eventDTO.put("name", "event");
        eventDTO.put("teamLimit", "10");

        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(eventDTO).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on

        Assert.assertEquals(1, eventRepository.count());
    }

    @Test
    public void testAddEventMissingTeamLimit() {
        Map<String, String> eventDTO = new HashMap<>();
        eventDTO.put("name", "event");
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
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on

        Assert.assertEquals(1, eventRepository.count());
    }

    @Test
    public void testAddEventEmptyBody() {
        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            post(EVENTS_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
        //@formatter:on

        Assert.assertEquals(1, eventRepository.count());
    }

    //endregion

    //region Test Change Events

    @Test
    public void testChangeEventAsAnon() {
        Map<String, String> eventDTO = new HashMap<>();
        eventDTO.put("name", "Changed Event");
        eventDTO.put("teamSize", "3");
        eventDTO.put("teamLimit", "11");

        //@formatter:off
        given().
        when().
            content(eventDTO).contentType(ContentType.JSON).
            put(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on

        Event changedEvent = eventRepository.findOne(testEvent.getId());

        Assert.assertEquals(testEvent.getEventName(), changedEvent.getEventName());
    }

    @Test
    public void testChangeEventAsUser() {
        Map<String, String> eventDTO = new HashMap<>();
        eventDTO.put("name", "Changed Event");
        eventDTO.put("teamSize", "3");
        eventDTO.put("teamLimit", "11");

        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(eventDTO).contentType(ContentType.JSON).
            put(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on

        Event changedEvent = eventRepository.findOne(testEvent.getId());
        Assert.assertEquals(testEvent.getEventName(), changedEvent.getEventName());
    }

    @Test
    public void testChangeEventAsAdmin() {
        Map<String, String> eventDTO = new HashMap<>();
        eventDTO.put("name", "Changed Event");
        eventDTO.put("teamSize", "3");
        eventDTO.put("teamLimit", "11");

        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(eventDTO).contentType(ContentType.JSON).
            put(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.eventName", is(eventDTO.get("name"))).
            body("object.teamSize", is(Integer.valueOf(eventDTO.get("teamSize")))).
            body("object.teamLimit", is(Integer.valueOf(eventDTO.get("teamLimit"))));
        //@formatter:on

        Event changedEvent = eventRepository.findOne(testEvent.getId());
        Assert.assertEquals(eventDTO.get("name"), changedEvent.getEventName());
    }

    @Test
    public void testChangeEventNameOnly() {
        Map<String, String> eventDTO = new HashMap<>();
        eventDTO.put("name", "Changed Event");

        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(eventDTO).contentType(ContentType.JSON).
            put(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.eventName", is(eventDTO.get("name"))).
            body("object.teamSize", equalTo(testEvent.getTeamSize())).
            body("object.teamLimit", equalTo(testEvent.getTeamLimit()));
        //@formatter:on

        Event changedEvent = eventRepository.findOne(testEvent.getId());
        Assert.assertEquals(eventDTO.get("name"), changedEvent.getEventName());
    }

    @Test
    public void testChangeEventLimitOnly() {
        Map<String, String> eventDTO = new HashMap<>();
        eventDTO.put("teamLimit", "11");

        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(eventDTO).contentType(ContentType.JSON).
            put(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.eventName", is(testEvent.getEventName())).
            body("object.teamSize", equalTo(testEvent.getTeamSize())).
            body("object.teamLimit", equalTo(Integer.valueOf(eventDTO.get("teamLimit"))));
        //@formatter:on

        Event changedEvent = eventRepository.findOne(testEvent.getId());
        Assert.assertEquals(eventDTO.get("teamLimit"), String.valueOf(changedEvent.getTeamLimit()));
    }

    @Test
    public void testChangeEventTeamSizeOnly() {
        Map<String, String> eventDTO = new HashMap<>();
        eventDTO.put("teamSize", "3");

        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(eventDTO).contentType(ContentType.JSON).
            put(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.eventName", is(testEvent.getEventName())).
            body("object.teamSize", equalTo(Integer.valueOf(eventDTO.get("teamSize")))).
            body("object.teamLimit", equalTo(testEvent.getTeamLimit()));
        //@formatter:on

        Event changedEvent = eventRepository.findOne(testEvent.getId());
        Assert.assertEquals(eventDTO.get("teamSize"), String.valueOf(changedEvent.getTeamSize()));
    }

    @Test
    public void testChangeEventNonExistingId() {
        Map<String, String> eventDTO = new HashMap<>();
        eventDTO.put("name", "Changed Event");
        eventDTO.put("teamSize", "3");
        eventDTO.put("teamLimit", "11");

        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(eventDTO).contentType(ContentType.JSON).
            put(EVENTS_ENDPOINT + "/999").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }
    //endregion

    //region Test Delete Events
    @Test
    public void testDeleteEventAsAnon() {
        //@formatter:off
        given().
        when().
            delete(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on

        Assert.assertEquals(1, eventRepository.count());
    }

    @Test
    public void testDeleteEventAsUser() {
        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            delete(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on

        Assert.assertEquals(1, eventRepository.count());
    }

    @Test
    public void testDeleteEventAsAdmin() {
        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            delete(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        Assert.assertEquals(0, eventRepository.count());
    }

    @Test
    public void testDeleteWithoutEventId() {
        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            delete(EVENTS_ENDPOINT + "/").
        then().
            statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
        //@formatter:on

        Assert.assertEquals(1, eventRepository.count());
    }

    @Test
    public void testDeleteNonExistingId() {
        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            delete(EVENTS_ENDPOINT + "/999").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on

        Assert.assertEquals(1, eventRepository.count());
    }
    //endregion

    //region Test Register Teams
    @Test
    public void testRegisterTeamAsAnon() {
        //@formatter:off
        given().
        when().
            content(team1.getId()).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on

    }

    @Test
    public void testRegisterTeamAsUser() {
        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team1.getId()).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testRegisterAsTeamMember() {
        SessionData login = login("user");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team2.getId()).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testRegisterAsTeamCaptain() {
        SessionData login = login("captain");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team2.getId()).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testRegisterAsAdmin() {
        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team2.getId()).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testRegisterForClosedEvent() {
        Event event = new Event("event", 1, 1);
        event.setOpenForRegistration(false);
        eventRepository.save(event);

        SessionData login = login("captain");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team1.getId()).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT + "/" + event.getId()).
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testRegisterWrongTeamSize() {
        SessionData login = login("captain");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team1.getId()).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testRegisterTeamLimitReached() {
        Team team = teamRepository.save(new Team("team3", user));
        Event event = new Event("event", 1, 1);
        event.addTeam(team);
        event = eventRepository.save(event);

        SessionData login = login("captain");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team1.getId()).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT + "/" + event.getId()).
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testRegisterTeamNonExistingTeamId() {
        SessionData login = login("captain");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(999).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_NOT_FOUND);
        //@formatter:on
    }

    @Test
    public void testRegisterTeamNonExistingEventId() {
        SessionData login = login("captain");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team1.getId()).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT + "/999").
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testRegisterTeamAlreadyRegistered() {
        SessionData login = login("captain");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team2.getId()).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_OK);

        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team2.getId()).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testRegisterTeamWithMemberAlreadyRegistered() {
        Team team = teamRepository.save(new Team("team", admin));
        team.addMember(user);
        team = teamRepository.save(team);

        SessionData login = login("admin");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team.getId()).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_OK);

        logout();

        login = login("captain");

        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team2.getId()).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testRegisterTeamWithCaptainAlreadyRegistered() {
        team1.addMember(admin);
        team1 = teamRepository.save(team1);
        SessionData login = login("captain");

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team1.getId()).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_OK);

        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(team2.getId()).contentType(ContentType.JSON).
            post(EVENTS_ENDPOINT + "/" + testEvent.getId()).
        then().
            statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }
    //endregion
}
