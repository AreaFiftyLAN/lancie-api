package ch.wisv.areafiftylan;

import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.web.committee.model.CommitteeMember;
import ch.wisv.areafiftylan.web.committee.service.CommitteeMemberRepository;
import ch.wisv.areafiftylan.web.committee.service.CommitteeService;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.collection.IsEmptyCollection.empty;

/**
 * This class tests the endpoints that provide our website with it's information.
 */
public class WebIntegrationTest extends XAuthIntegrationTest {

    @Autowired
    CommitteeMemberRepository committeeMemberRepository;
    @Autowired
    CommitteeService committeeService;

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


    //region Committee

    @Test
    public void testAddCommitteeMemberAsUser() {
        User user = createUser();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(user)).
        when().
            body(committeeMember1).
            put("/web/committee/1").
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testAddCommitteeMemberAsAdmin() {
        User admin = createAdmin();

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(admin)).
        when().
            body(committeeMember1).
            put("/web/committee/1").
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
        committeeService.addCommitteeMember(committeeMember1);

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(user)).
        when().
            get("/web/committee").
        then().
            statusCode(HttpStatus.SC_OK).
            body("name", containsString("Lotte Bryan"));
        //@formatter:on
    }

    @Test
    public void testGetCommitteeAsUserMultiple() {
        User user = createUser();
        committeeService.addCommitteeMember(committeeMember1);
        committeeService.addCommitteeMember(committeeMember2);

        //@formatter:off
        given().
			header(getXAuthTokenHeaderForUser(user)).
        when().
            get("/web/committee").
        then().
            statusCode(HttpStatus.SC_OK).
            body("name", hasItems("Lotte Bryan", "Sterre Noorthoek"));
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

    }

    @Test
    public void testUpdateCommitteeMemberAsAdmin() {
        User admin = createAdmin();

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
