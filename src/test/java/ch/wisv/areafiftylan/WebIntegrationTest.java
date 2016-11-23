package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.utils.SessionData;
import ch.wisv.areafiftylan.web.model.CommitteeMember;
import ch.wisv.areafiftylan.web.service.CommitteeMemberRepository;
import com.jayway.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * This class tests the endpoints that provide our website with it's information.
 */
public class WebIntegrationTest extends IntegrationTest {

    @Autowired
    CommitteeMemberRepository committeeMemberRepository;

    private CommitteeMember committeeMember1 = new CommitteeMember(1L, "Lotte Bryan", "Chairman", "group");
    private CommitteeMember committeeMember2 = new CommitteeMember(2L, "Sterre Noorthoek", "Secretary", "male");
    private CommitteeMember committeeMember3 = new CommitteeMember(3L, "Francis Behnen", "Treasurer", "money");
    private CommitteeMember committeeMember4 = new CommitteeMember(4L, "Hilco van der Wilk", "Commissioner of Promo", "bullhorn");
    private CommitteeMember committeeMember5 = new CommitteeMember(5L, "Lotte Millen van Osch", "Commissioner of Logistics", "truck");
    private CommitteeMember committeeMember6 = new CommitteeMember(6L, "Matthijs Kok", "Commissioner of Systems", "cogs");
    private CommitteeMember committeeMember7 = new CommitteeMember(7L, "Beer van der Drift", "Qualitate Qua", "heart");
    private List<CommitteeMember> committeeMemberList = new ArrayList<>(Arrays.asList(
            committeeMember1, committeeMember2, committeeMember3, committeeMember4,
            committeeMember5, committeeMember6, committeeMember7));

    @Before
    public void initWebIntegrationTestMethod() {
        committeeMemberRepository.save(committeeMemberList);
    }

    @After
    public void cleanupWebIntegrationTestMethod() {
        committeeMemberRepository.deleteAll();
    }

    @Test
    public void testSetCommitteeDataAsUser() {
        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(committeeMemberList).contentType(ContentType.JSON).
            put("/web/committee").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testSetCommitteeDataAsAdmin() {
        SessionData login = login(admin.getUsername(), adminCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(committeeMemberList).contentType(ContentType.JSON).
            put("/web/committee").
        then().
            statusCode(HttpStatus.SC_CREATED).
            body("message", equalTo("Committee members saved successfully."));
        //@formatter:on
    }

    @Test
    public void testGetCommitteeDataAsUser() {
        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
                when().
                get("/web/committee").
                then().
                statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testGetCommitteeDataAsAdmin() {
        SessionData login = login(admin.getUsername(), adminCleartextPassword);

        //@formatter:off
        given().
                filter(sessionFilter).
                header(login.getCsrfHeader()).
                when().
                get("/web/committee").
                then().
                statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testAddCommitteeDataAsUser() {
        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(committeeMember1).contentType(ContentType.JSON).
            post("/web/committee").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testAddCommitteeDataAsAdmin() {
        SessionData login = login(admin.getUsername(), adminCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(committeeMember1).contentType(ContentType.JSON).
            post("/web/committee").
        then().
            statusCode(HttpStatus.SC_ACCEPTED).
            body("message", equalTo("Committee member added successfully."));
        //@formatter:on
    }
    @Test
    public void testDeleteCommitteeDataAsUser() {
        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            delete("/web/committee").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testDeleteCommitteeDataAsAdmin() {
        SessionData login = login(admin.getUsername(), adminCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            delete("/web/committee").
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("Committee members deleted successfully."));
        //@formatter:on
    }
}
