package ch.wisv.areafiftylan.integration;

import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.web.committee.model.CommitteeMember;
import ch.wisv.areafiftylan.web.committee.service.CommitteeRepository;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

public class WebCommitteeIntegrationTest extends XAuthIntegrationTest {

    @Autowired
    private CommitteeRepository committeeRepository;

    private final String COMMITTEE_ENDPOINT = "/web/committee/";

    private CommitteeMember addCommitteeMember() {
        CommitteeMember committeeMember = new CommitteeMember();
        committeeMember.setPosition(committeeRepository.count() + 1);
        committeeMember.setName("Lotte Bryan");
        committeeMember.setFunction("Chairman");
        committeeMember.setIcon("group");
        return committeeRepository.save(committeeMember);
    }

    @AfterEach
    public void cleanupCommitteeTest() {
        committeeRepository.deleteAll();
    }

    @Test
    public void testAddCommitteeMemberAsUser() {
        User user = createUser();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(user)).
        when().
            body(addCommitteeMember()).
            contentType(ContentType.JSON).
            post(COMMITTEE_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testAddCommitteeMemberAsAdmin() {
        User admin = createAdmin();
        CommitteeMember committeeMember = addCommitteeMember();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(admin)).
        when().
            body(committeeMember).
           contentType(ContentType.JSON).
         post(COMMITTEE_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_CREATED).
            body("message", equalTo("Successfully added committee member.")).
            body("object.name", equalTo("Lotte Bryan"));
        //@formatter:on
    }

    @Test
    public void testGetCommitteeAsUserEmpty() {
        User user = createUser();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(user)).
        when().
            get(COMMITTEE_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object", hasSize(0));
        //@formatter:on
    }

    @Test
    public void testGetCommitteeAsUserSingle() {
        User user = createUser();
        addCommitteeMember();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(user)).
        when().
            get(COMMITTEE_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.name", hasItem("Lotte Bryan")).
            body("object", hasSize(1));
        //@formatter:on
    }

    @Test
    public void testGetCommitteeAsUserMultiple() {
        User user = createUser();
        CommitteeMember committeeMember1 = addCommitteeMember();
        CommitteeMember committeeMember2 = addCommitteeMember();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(user)).
        when().
            get(COMMITTEE_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.name", hasItem("Lotte Bryan")).
            body("object", hasSize(2));
        //@formatter:on
    }

    @Test
    public void testGetCommitteeAsAdmin() {
        User admin = createAdmin();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(admin)).
        when().
            get(COMMITTEE_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object", hasSize(0));
        //@formatter:on
    }

    @Test
    public void testDeleteCommitteeMemberAsUser() {
        User user = createUser();
        CommitteeMember member = addCommitteeMember();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(user)).
        when().
            delete(COMMITTEE_ENDPOINT + member.getPosition()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testDeleteCommitteeMemberAsAdmin() {
        User admin = createAdmin();
        CommitteeMember member = addCommitteeMember();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(admin)).
        when().
            delete(COMMITTEE_ENDPOINT + member.getPosition()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("Successfully removed committee member."));
        //@formatter:on

    }

    @Test
    public void testDeleteCommitteeAsUser() {
        User user = createUser();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(user)).
        when().
            delete(COMMITTEE_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testDeleteCommitteeAsAdmin() {
        User admin = createAdmin();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(admin)).
        when().
            delete(COMMITTEE_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("Successfully removed committee."));
        //@formatter:on
    }
}
