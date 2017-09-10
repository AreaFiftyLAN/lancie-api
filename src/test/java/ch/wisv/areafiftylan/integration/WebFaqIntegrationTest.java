package ch.wisv.areafiftylan.integration;

import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.web.faq.model.FaqPair;
import ch.wisv.areafiftylan.web.faq.service.FaqRepository;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

public class WebFaqIntegrationTest  extends XAuthIntegrationTest{

    @Autowired
    private FaqRepository faqRepository;

    private final String FAQ_ENDPOINT = "/web/faq/";

    private FaqPair createPair() {
        FaqPair faqPair = new FaqPair();
        faqPair.setQuestion("question");
        faqPair.setAnswer("answer");
        return faqRepository.save(faqPair);
    }

    @After
    public void cleanupFaqTests() {
        faqRepository.deleteAll();
    }

    @Test
    public void testGetFaqAsUser() {
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(FAQ_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testGetFaqAsCommitteeMember() {
        User committeeMember = createCommitteeMember();
        FaqPair faqPair = createPair();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(committeeMember)).
        when().
            get(FAQ_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", is("FAQ retrieved.")).
            body("object.question", hasItem(faqPair.getQuestion()));
        //@formatter:on
    }

    @Test
    public void testAddQuestionAsUser() {
        User user = createUser();
        FaqPair faqPair = createPair();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(faqPair).
            contentType(ContentType.JSON).
        when().
            post(FAQ_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testAddQuestionAsCommitteeMember() {
        User committeeMember = createCommitteeMember();
        FaqPair faqPair = createPair();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(committeeMember)).
            body(faqPair).
            contentType(ContentType.JSON).
        when().
            post(FAQ_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_CREATED).
            body("message", is("FaqPair added.")).
            body("object.question", is(faqPair.getQuestion()));
        //@formatter:on
    }

    @Test
    public void testDeleteQuestionAsUser() {
        User user = createUser();
        FaqPair faqPair = createPair();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(faqPair).
            contentType(ContentType.JSON).
        when().
            delete(FAQ_ENDPOINT + faqPair.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testDeleteQuestionAsCommitteeMember() {
        User committeeMember = createCommitteeMember();
        FaqPair faqPair = createPair();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(committeeMember)).
            body(faqPair).
            contentType(ContentType.JSON).
        when().
            delete(FAQ_ENDPOINT + faqPair.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", is("FaqPair deleted."));
        //@formatter:on
    }

    @Test
    public void testDeleteFaqAsUser() {
        User user = createUser();
        FaqPair faqPair = createPair();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(faqPair).
            contentType(ContentType.JSON).
        when().
            delete(FAQ_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testDeleteFaqAsCommitteeMember() {
        User committeeMember = createCommitteeMember();
        FaqPair faqPair = createPair();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(committeeMember)).
            body(faqPair).
            contentType(ContentType.JSON).
        when().
            delete(FAQ_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", is("Faq deleted."));
        //@formatter:on
    }


}
