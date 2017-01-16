package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.utils.SessionData;
import ch.wisv.areafiftylan.web.committee.model.CommitteeMember;
import ch.wisv.areafiftylan.web.committee.service.CommitteeMemberRepository;
import com.jayway.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;

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
    private ArrayList<CommitteeMember> committeeMemberList = new ArrayList<>(Arrays.asList(
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
    public void testSetCommitteeAsUser() {
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
    public void testSetCommitteeAsAdmin() {
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
            body("message", equalTo("Committee saved successfully."));
        //@formatter:on
    }

    @Test
    public void testGetCommitteeAsUser() {
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
    public void testGetCommitteeAsAdmin() {
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
    public void testDeleteCommitteeAsUser() {
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
    public void testDeleteCommitteeAsAdmin() {
        SessionData login = login(admin.getUsername(), adminCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            delete("/web/committee").
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("Committee deleted successfully."));
        //@formatter:on
    }

    @Test
    public void testAddCommitteeMemberAsUser() {
        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(committeeMember1).contentType(ContentType.JSON).
            put("/web/committee/1").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testAddCommitteeMemberAsAdmin() {
        SessionData login = login(admin.getUsername(), adminCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            content(committeeMember1).contentType(ContentType.JSON).
            put("/web/committee/1").
        then().
            statusCode(HttpStatus.SC_ACCEPTED).
            body("message", equalTo("Committee member added successfully."));
        //@formatter:on
    }

    @Test
    public void testGetCommitteeMemberAsUser() {
        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/web/committee/1").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testGetCommitteeMemberAsAdmin() {
        SessionData login = login(admin.getUsername(), adminCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            get("/web/committee/1").
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testDeleteCommitteeMemberAsUser() {
        SessionData login = login(user.getUsername(), userCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            delete("/web/committee/1").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testDeleteCommitteeMemberAsAdmin() {
        SessionData login = login(admin.getUsername(), adminCleartextPassword);

        //@formatter:off
        given().
            filter(sessionFilter).
            header(login.getCsrfHeader()).
        when().
            delete("/web/committee/1").
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("Committee member deleted successfully."));
        //@formatter:on
    }

}
