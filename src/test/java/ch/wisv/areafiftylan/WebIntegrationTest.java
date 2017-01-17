package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.web.committee.model.CommitteeMember;
import ch.wisv.areafiftylan.web.committee.service.CommitteeMemberRepository;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

/**
 * This class tests the endpoints that provide our website with it's information.
 */
public class WebIntegrationTest extends XAuthIntegrationTest {

    @Autowired
    protected CommitteeMemberRepository committeeMemberRepository;

//    private CommitteeMember committeeMember1 = new CommitteeMember(1L, "Lotte Bryan", "Chairman", "group");
//    private CommitteeMember committeeMember2 = new CommitteeMember(2L, "Sterre Noorthoek", "Secretary", "male");

    private CommitteeMember createCommitteeMember() {
        CommitteeMember committeeMember = new CommitteeMember(1L, "Lotte Bryan", "Chairman", "group");
        return committeeMemberRepository.save(committeeMember);
    }

    //region Committee

    @Test
    public void testAddCommitteeMemberAsUser() {
        User user = createUser();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(user)).
        when().
            body(createCommitteeMember()).
            post("/web/committee").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testAddCommitteeMemberAsAdmin() {
        User admin = createAdmin();
        CommitteeMember committeeMember = createCommitteeMember();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(admin)).
        when().
            body(committeeMember).
            post("/web/committee").
        then().
            statusCode(HttpStatus.SC_CREATED).
            body("message", equalTo("Committee member added successfully.")).
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
            get("/web/committee").
        then().
            statusCode(HttpStatus.SC_OK).
            body(empty());
        //@formatter:on
    }

    @Test
    public void testGetCommitteeAsUserSingle() {
        User user = createUser();
        createCommitteeMember();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(user)).
        when().
            get("/web/committee").
        then().
            statusCode(HttpStatus.SC_OK).
            body("name", hasItem("Lotte Bryan")).
            body("$", hasSize(1));
        //@formatter:on
    }

    @Test
    public void testGetCommitteeAsUserMultiple() {
        User user = createUser();
        CommitteeMember committeeMember1 = createCommitteeMember();
        CommitteeMember committeeMember2 = createCommitteeMember();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(user)).
        when().
            get("/web/committee").
        then().
            statusCode(HttpStatus.SC_OK).
            body("name", hasItem("Lotte Bryan")).
            body("$", hasSize(2));
        //@formatter:on
    }

    @Test
    public void testGetCommitteeAsAdmin() {
        User admin = createAdmin();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(admin)).
        when().
            get("/web/committee").
        then().
            statusCode(HttpStatus.SC_OK).
            body(empty());
        //@formatter:on
    }

    @Test
    public void testUpdateCommitteeMemberAsUser() {
        User user = createUser();
        CommitteeMember committeeMember1 = createCommitteeMember();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(user)).
        when().
            body(committeeMember1).
            put("/web/committee/" + committeeMember1.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testUpdateCommitteeMemberAsAdmin() {
        User admin = createAdmin();
        CommitteeMember committeeMember1 = createCommitteeMember();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(admin)).
        when().
            body(committeeMember1).
            put("/web/committee").
        then().
            statusCode(HttpStatus.SC_CREATED).
            body("message", equalTo("Committee member added successfully.")).
            body("object.name", equalTo("Lotte Bryan"));
        //@formatter:on
    }

    @Test
    public void testDeleteCommitteeMemberAsUser() {
        User user = createUser();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(user)).
        when().
            delete("/web/committee/1").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testDeleteCommitteeMemberAsAdmin() {
        User admin = createAdmin();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(admin)).
        when().
            delete("/web/committee/1").
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", equalTo("Committee member deleted successfully."));
        //@formatter:on
    }

    //endregion

}
