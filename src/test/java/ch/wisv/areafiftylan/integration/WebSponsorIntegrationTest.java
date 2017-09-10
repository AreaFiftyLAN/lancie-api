package ch.wisv.areafiftylan.integration;

import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.web.sponsor.model.Sponsor;
import ch.wisv.areafiftylan.web.sponsor.model.SponsorType;
import ch.wisv.areafiftylan.web.sponsor.service.SponsorRepository;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class WebSponsorIntegrationTest extends XAuthIntegrationTest {

    @Autowired
    private SponsorRepository sponsorRepository;

    private final String SPONSOR_ENDPOINT = "/web/sponsor/";
    private final String TYPE = "PREMIUM";

    private Sponsor createSponsor() {
        Sponsor sponsor = new Sponsor();
        sponsor.setName("Red Bull");
        sponsor.setImageName("redbulllogo.png");
        sponsor.setWebsite("www.redbull.com");
        sponsor.setType(SponsorType.PREMIUM);
        return sponsorRepository.save(sponsor);
    }

    @After
    public void cleanupSponsorTests() {
        sponsorRepository.deleteAll();
    }

    @Test
    public void testCreateSponsorAsUser() {
        User user = createUser();
        Sponsor sponsor = createSponsor();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(sponsor).
            contentType(ContentType.JSON).
        when().
            post(SPONSOR_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testCreateSponsorAsCommitteeMember() {
        User committeeMember = createCommitteeMember();
        Sponsor sponsor = createSponsor();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(committeeMember)).
            body(sponsor).
            contentType(ContentType.JSON).
        when().
            post(SPONSOR_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_CREATED).
            body("object.name", is(sponsor.getName()));
        //@formatter:on
    }

    @Test
    public void testGetSponsorsAsUser() {
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(SPONSOR_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testGetSponsorsAsCommitteeMember() {
        User committeeMember = createCommitteeMember();
        Sponsor sponsor = createSponsor();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(committeeMember)).
        when().
            get(SPONSOR_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.name", hasItem(sponsor.getName()));
        //@formatter:on
    }

    @Test
    public void testGetSponsorOfTypeAsUserEmpty() {
        User user = createUser();
        Sponsor sponsor = createSponsor();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(SPONSOR_ENDPOINT + "NORMAL").
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.name", not(contains(sponsor.getName())));
        //@formatter:on
    }

    @Test
    public void testGetSponsorOfTypeAsUser() {
        User user = createUser();
        Sponsor sponsor = createSponsor();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(SPONSOR_ENDPOINT + TYPE).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.name", contains(sponsor.getName()));
        //@formatter:on
    }

    @Test
    public void testDeleteSponsorAsUser() {
        User user = createUser();
        Sponsor sponsor = createSponsor();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            delete(SPONSOR_ENDPOINT + sponsor.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testDeleteSponsorAsCommitteeMember() {
        User admin = createAdmin();
        Sponsor sponsor = createSponsor();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            delete(SPONSOR_ENDPOINT + sponsor.getId()).
        then().
            statusCode(HttpStatus.SC_NO_CONTENT);
        //@formatter:on
    }

    @Test
    public void testDeleteAllSponsorsAsUser() {
        User user = createUser();
        Sponsor sponsor = createSponsor();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            delete(SPONSOR_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testDeleteAllSponsorsAsCommitteeMember() {
        User admin = createAdmin();
        Sponsor sponsor = createSponsor();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            delete(SPONSOR_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_NO_CONTENT);
        //@formatter:on
    }
}
