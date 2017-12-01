package ch.wisv.areafiftylan.integration;

import ch.wisv.areafiftylan.users.model.User;
import ch.wisv.areafiftylan.web.banner.model.Banner;
import ch.wisv.areafiftylan.web.banner.service.BannerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class WebBannerIntegrationTest extends XAuthIntegrationTest {

    @Autowired
    private BannerRepository bannerRepository;

    private static final String BANNER_ENDPOINT = "/web/banners/";

    private Banner banner;
    private Collection<Banner> banners;
    private User user;
    private User admin;

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        banner = new Banner();
        banners = new ArrayList<>();
        banner.setText("AreaFiftyLAN starts in less than a month! Make sure to get your tickets!");
        banner.setStartDate(Date.valueOf("2018-02-03"));
        banner.setEndDate(Date.valueOf("2018-02-28"));
        banners.add(bannerRepository.save(banner));

        user = createUser();
        admin = createAdmin();

        mapper = new ObjectMapper();
    }

    @After
    public void cleanUpBanners() {
        bannerRepository.deleteAll();
    }

    //region GET all banners
    @Test
    public void testGetBannersWithoutPermissions() {
        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(BANNER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testGetBannerWithPermissions() throws JsonProcessingException {
        String expected = mapper.writeValueAsString(banners);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            get(BANNER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK).
            body(equalTo(expected));
        //@formatter:on
    }

    //endregion
    //region GET current banner
    @Test
    public void testGetCurrentBanner() throws JsonProcessingException {
        LocalDate lastWeek = LocalDate.now().minusWeeks(1);
        LocalDate nextWeek = LocalDate.now().plusWeeks(1);
        Banner banner = new Banner();
        banner.setStartDate(Date.valueOf(lastWeek));
        banner.setEndDate(Date.valueOf(nextWeek));
        banner.setText("Testing current banner");

        bannerRepository.save(banner);

        String expected = mapper.writeValueAsString(banner);

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(BANNER_ENDPOINT + "current").
        then().
            statusCode(HttpStatus.SC_OK).
            body(equalTo(expected));
        //@formatter:on
    }

    @Test
    public void testNoCurrentBanner() {
        bannerRepository.deleteAll();

        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            get(BANNER_ENDPOINT + "current").
        then().
            statusCode(HttpStatus.SC_NOT_FOUND);
        //@formatter:on
    }

    //endregion
    //region POST add banner
    @Test
    public void testAddBannerWithoutPermissions() {
        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            body(banner).
            contentType(ContentType.JSON).
            post(BANNER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testAddBannerWithPermissions() {
        int oldSize = bannerRepository.findAll().size();

        Banner newBanner = new Banner();
        newBanner.setText("Welcome to AreaFiftyLan");
        newBanner.setStartDate(Date.valueOf("2018-03-03"));
        newBanner.setEndDate(Date.valueOf("2018-03-28"));
        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            body(newBanner).
            contentType(ContentType.JSON).
            post(BANNER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        assertEquals(oldSize + 1, bannerRepository.findAll().size());
    }

    //endregion
    //region POST update banner
    @Test
    public void testUpdateBannerWithoutPermissions() {
        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            body(banner).
            contentType(ContentType.JSON).
            post(BANNER_ENDPOINT + 1).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testUpdateBannerWithPermissions() {
        Banner banner = bannerRepository.findAll().get(0);
        banner.setText("Updated text");
        System.out.println(banner);
        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            body(banner).
            contentType(ContentType.JSON).
            post(BANNER_ENDPOINT + banner.getId()).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        assertEquals(banner.getText(), bannerRepository.findOne(banner.getId()).getText());
    }

    @Test
    public void testUpdateBannerNotFound() {
        bannerRepository.deleteAll();
        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            body(banner).
            contentType(ContentType.JSON).
            post(BANNER_ENDPOINT + 1).
        then().
            statusCode(HttpStatus.SC_NOT_FOUND);
        //@formatter:on
    }

    //endregion
    //region DELETE banner
    @Test
    public void testDeleteBannerWithoutPermissions() {
        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            delete(BANNER_ENDPOINT + 1).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testDeleteBannerWithPermissions() {
        Long id = bannerRepository.findAll().get(0).getId();
        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            delete(BANNER_ENDPOINT + id).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        assertFalse(bannerRepository.exists(id));
    }
    //endregion
    //region DELETE all banners

    @Test
    public void testDeleteAllBannersWithoutPermissions() {
        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(user)).
        when().
            delete(BANNER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_FORBIDDEN).
            body("message", equalTo("Access denied"));
        //@formatter:on
    }

    @Test
    public void testDeleteAllBannersWithPermissions() {
        //@formatter:off
        given().
            header(getXAuthTokenHeaderForUser(admin)).
        when().
            delete(BANNER_ENDPOINT).
        then().
            statusCode(HttpStatus.SC_OK);
        //@formatter:on

        assertEquals(0, bannerRepository.findAll().size());
    }
    //endregion
}
