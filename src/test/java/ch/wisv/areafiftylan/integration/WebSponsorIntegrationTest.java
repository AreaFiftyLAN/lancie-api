package ch.wisv.areafiftylan.integration;

import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.web.sponsor.model.Sponsor;
import ch.wisv.areafiftylan.web.sponsor.model.SponsorType;
import ch.wisv.areafiftylan.web.sponsor.service.SponsorRepository;
import ch.wisv.areafiftylan.web.tournament.model.Tournament;
import ch.wisv.areafiftylan.web.tournament.service.TournamentRepository;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

public class WebSponsorIntegrationTest extends XAuthIntegrationTest {

    @Autowired
    private SponsorRepository sponsorRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    private final String SPONSOR_ENDPOINT = "/web/sponsor/";
    private final String TYPE = "PREMIUM";

    private Sponsor createSponsor() {
        Sponsor sponsor = new Sponsor();
        sponsor.setName("Red Bull");
        sponsor.setImageName("redbulllogo.png");
        sponsor.setWebsite("www.redbull.com");
        sponsor.setType(SponsorType.PREMIUM);
        sponsor.setTournaments(new HashSet<>());
        return sponsorRepository.save(sponsor);
    }

    private Sponsor updateSponsor() {
        Sponsor sponsor = new Sponsor();
        sponsor.setName("CH");
        sponsor.setImageName("epicowl.png");
        sponsor.setWebsite("www.thuischbezorgd.nl");
        sponsor.setType(SponsorType.PRESENTER);
        sponsor.setTournaments(new HashSet<>());
        return sponsor;
    }

    private Tournament createSponsorUnderTournament() {
        Sponsor sponsor = new Sponsor();
        sponsor.setName("TU Delft");
        sponsor.setImageName("lameimage.png");
        sponsor.setWebsite("http://tudelft.nl");
        sponsor.setType(SponsorType.NORMAL);
        sponsor = sponsorRepository.save(sponsor);

        Tournament tournament = new Tournament();
        tournament.setHeaderTitle("A tournament");
        tournament.setSponsor(sponsor);
        return tournamentRepository.save(tournament);
    }

    @After
    public void cleanupSponsorTests() {
        tournamentRepository.deleteAll();
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
            statusCode(HttpStatus.SC_OK).
            body("message", is("Sponsor successfully deleted."));
        //@formatter:on
    }

    @Test
    public void testDeleteAllSponsorsAsUser() {
        User user = createUser();

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

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            delete(SPONSOR_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", is("Successfully deleted all sponsors."));
        //@formatter:on
    }

    @Test
    public void testTryUpdateSponsorAsUser() {
        User user = createUser();
        Sponsor sponsor = createSponsor(),
                updateSponsor = updateSponsor();

        updateSponsor.setId(sponsor.getId());

        given()
            .header(getXAuthTokenHeaderForUser(user))
            .body(updateSponsor)
            .contentType(ContentType.JSON)
        .when()
            .post(SPONSOR_ENDPOINT)
        .then()
            .statusCode(HttpStatus.SC_FORBIDDEN);

        assertEquals(1, sponsorRepository.findAll().size());
        assertEquals(sponsor, sponsorRepository.findAll().get(0));
    }

    @Test
    public void testTryUpdateSponsorAsCommittee() {
        User committee = createCommitteeMember();
        Sponsor sponsor = createSponsor(),
                updateSponsor = updateSponsor();

        updateSponsor.setId(sponsor.getId());

        given()
            .header(getXAuthTokenHeaderForUser(committee))
            .body(updateSponsor)
            .contentType(ContentType.JSON)
        .when()
            .post(SPONSOR_ENDPOINT)
        .then()
            .statusCode(HttpStatus.SC_CREATED);

        assertEquals(1, sponsorRepository.findAll().size());
        assertEquals(updateSponsor, sponsorRepository.findById(updateSponsor.getId()).orElse(new Sponsor()));
    }

    @Test
    public void testTryDeleteUsedSponsor() {
        User admin = createAdmin();
        Tournament tournamentWithSponsor = createSponsorUnderTournament();

        given()
            .header(getXAuthTokenHeaderForUser(admin))
        .when()
            .delete(SPONSOR_ENDPOINT + tournamentWithSponsor.getSponsor().getId())
        .then()
            .statusCode(HttpStatus.SC_CONFLICT)
            .body("message", is("Sponsor TU Delft is still used by tournaments: A tournament"));
    }
}
