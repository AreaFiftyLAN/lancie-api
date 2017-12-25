package ch.wisv.areafiftylan.integration;

import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.web.sponsor.model.Sponsor;
import ch.wisv.areafiftylan.web.sponsor.model.SponsorType;
import ch.wisv.areafiftylan.web.sponsor.service.SponsorRepository;
import ch.wisv.areafiftylan.web.tournament.model.Tournament;
import ch.wisv.areafiftylan.web.tournament.model.TournamentType;
import ch.wisv.areafiftylan.web.tournament.service.TournamentRepository;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

public class WebTournamentIntegrationTest extends XAuthIntegrationTest {

    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private SponsorRepository sponsorRepository;

    private final String TOURNAMENT_ENDPOINT = "/web/tournament/";
    private final String TYPE = "OFFICIAL";

    private Tournament createTournament() {
        Tournament tournament = new Tournament();
        tournament.setType(TournamentType.OFFICIAL);
        tournament.setButtonTitle("RL");
        tournament.setButtonImagePath("/images/activities/rl_logo.png");
        tournament.setFormat("3 v 3");
        tournament.setHeaderTitle("Rocket League");
        tournament.setDescription("Rocket League is een leuk spelletje en de regels zijn er niet.");
        tournament.setPrizes(Arrays.asList("Eerste Prijs", "Tweede Prijs", "Derde Prijs"));

        Sponsor sponsor = new Sponsor();
        sponsor.setName("Red Bull");
        sponsor.setImageName("redbulllogo.png");
        sponsor.setWebsite("www.redbull.com");
        sponsor.setType(SponsorType.PREMIUM);
        sponsorRepository.save(sponsor);

        tournament.setSponsor(sponsor);
        return tournamentRepository.save(tournament);
    }

    private Tournament updateTournament() {
        Tournament tournament = new Tournament();
        tournament.setType(TournamentType.UNOFFICIAL);
        tournament.setButtonTitle("AB");
        tournament.setButtonImagePath("imgpath");
        tournament.setFormat("1 v 1");
        tournament.setHeaderTitle("AlphaBetus");
        tournament.setDescription("AlphaBetus lorum ipsum");
        tournament.setPrizes(Arrays.asList("one prize", "two prize"));

        Sponsor sponsor = new Sponsor();
        sponsor.setName("Sponsorzzz");
        sponsor.setImageName("sponzor.png");
        sponsor.setWebsite("www.sponzor.com");
        sponsor.setType(SponsorType.NORMAL);
        sponsorRepository.save(sponsor);

        tournament.setSponsor(sponsor);
        return tournament;
    }

    @After
    public void cleanupTournamentTests() {
        tournamentRepository.deleteAll();
    }


    @Test
    public void testCreateTournamentAsUser() {
        User user = createUser();
        Tournament tournament = createTournament();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
            body(tournament).
            contentType(ContentType.JSON).
        when().
            post(TOURNAMENT_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testCreateTournamentAsCommitteeMember() {
        User committeeMember = createCommitteeMember();
        Tournament tournament = createTournament();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(committeeMember)).
            body(tournament).
            contentType(ContentType.JSON).
        when().
            post(TOURNAMENT_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_CREATED).
            body("object.headerTitle", is(tournament.getHeaderTitle()));
        //@formatter:on
    }

    @Test
    public void testGetTournamentsAsUser() {
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(TOURNAMENT_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on
    }

    @Test
    public void testGetTournamentsAsCommitteeMember() {
        User committeeMember = createCommitteeMember();
        Tournament tournament = createTournament();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(committeeMember)).
        when().
            get(TOURNAMENT_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.headerTitle", hasItem(tournament.getHeaderTitle()));
        //@formatter:on
    }

    @Test
    public void testGetTournamentOfTypeAsUserEmpty() {
        User user = createUser();
        Tournament tournament = createTournament();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(TOURNAMENT_ENDPOINT + "UNOFFICIAL").
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.headerTitle", not(contains(tournament.getHeaderTitle())));
        //@formatter:on
    }

    @Test
    public void testGetTournamentOfTypeAsUser() {
        User user = createUser();
        Tournament tournament = createTournament();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(TOURNAMENT_ENDPOINT + TYPE).
        then().
            statusCode(HttpStatus.SC_OK).
            body("object.headerTitle", contains(tournament.getHeaderTitle()));
        //@formatter:on
    }

    @Test
    public void testDeleteTournamentAsUser() {
        User user = createUser();
        Tournament tournament = createTournament();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            delete(TOURNAMENT_ENDPOINT + tournament.getId()).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testDeleteTournamentAsCommitteeMember() {
        User admin = createAdmin();
        Tournament tournament = createTournament();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            delete(TOURNAMENT_ENDPOINT + tournament.getId()).
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", is("Tournament deleted."));
        //@formatter:on
    }

    @Test
    public void testDeleteAllTournamentsAsUser() {
        User user = createUser();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            delete(TOURNAMENT_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN);
        //@formatter:on
    }

    @Test
    public void testDeleteAllTournamentsAsCommitteeMember() {
        User admin = createAdmin();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            delete(TOURNAMENT_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body("message", is("All Tournaments deleted."));
        //@formatter:on
    }

    @Test
    public void testUpdateTournamentAsUser() {
        User user = createUser();
        Tournament tournament = createTournament(),
                   updateTournament = updateTournament();
        updateTournament.setId(tournament.getId());

        given()
            .header(getXAuthTokenHeaderForUser(user))
            .body(updateTournament)
            .contentType(ContentType.JSON)
        .when()
            .put(TOURNAMENT_ENDPOINT + tournament.getId())
        .then()
            .statusCode(HttpStatus.SC_FORBIDDEN);

        assertEquals(1, tournamentRepository.findAll().size());
        assertEquals(tournament, tournamentRepository.findOne(tournament.getId()));
    }

    @Test
    public void testUpdateTournamentAsCommittee() {
        User committee = createCommitteeMember();
        Tournament tournament = createTournament(),
                   updateTournament = updateTournament();
        updateTournament.setId(tournament.getId());

        given()
            .header(getXAuthTokenHeaderForUser(committee))
            .body(updateTournament)
            .contentType(ContentType.JSON)
        .when()
            .put(TOURNAMENT_ENDPOINT + tournament.getId())
        .then()
            .statusCode(HttpStatus.SC_CREATED);

        assertEquals(1, tournamentRepository.findAll().size());
        assertEquals(updateTournament, tournamentRepository.findOne(tournament.getId()));
    }

    @Test
    public void testUpdateUnknownTournament() {
        User admin = createAdmin();
        Tournament updateTournament = updateTournament();
        updateTournament.setId(123L);

        assertEquals(0, tournamentRepository.findAll().size());

        given()
            .header(getXAuthTokenHeaderForUser(admin))
            .body(updateTournament)
            .contentType(ContentType.JSON)
        .when()
            .put(TOURNAMENT_ENDPOINT + updateTournament.getId())
        .then()
            .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
            .body("message", equalTo("Could not find tournament"));

        assertEquals(0, tournamentRepository.findAll().size());
    }
}
