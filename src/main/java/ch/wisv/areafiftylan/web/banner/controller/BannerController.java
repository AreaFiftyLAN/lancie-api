package ch.wisv.areafiftylan.web.banner.controller;

import ch.wisv.areafiftylan.exception.BannerNotFoundException;
import ch.wisv.areafiftylan.web.banner.model.Banner;
import ch.wisv.areafiftylan.web.banner.service.BannerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@Controller
@RequestMapping("/web/banners")
public class BannerController {

    private BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    /**
     * Get all banners in the database.
     *
     * @return all banners
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getBanners() {
        Collection<Banner> banners = bannerService.getAllBanners();
        return new ResponseEntity<>(banners, HttpStatus.OK);
    }

    /**
     * Returns a banner where the current date (date when the request was made)
     * lies within the range of its start- and end-date.
     *
     * @return a banner
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentBanner() {
        Banner currentBanner = bannerService.getCurrentbanner();
        return new ResponseEntity<>(currentBanner, HttpStatus.OK);
    }

    /**
     * Adds a new banner to the database. The date-range should be valid (e.g. there shouldn't be
     * another entity covering that range already).
     *
     * @param banner banner to be added
     * @return a response-entity
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> addBanner(@RequestBody Banner banner) {
        banner = bannerService.addBanner(banner);
        return createResponseEntity(HttpStatus.OK,
                "Successfully created a banner with ID=" + banner.getId(), banner);
    }

    /**
     * Updates the banner with the given Id. The date-range should be valid (e.g. there shouldn't be
     * another entity covering that range already).
     *
     * @param bannerId Id of the banner to be updated
     * @param banner   banner to be updated
     * @return the updated banner
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{bannerId}")
    public ResponseEntity<?> updateBanner(@PathVariable Long bannerId, @RequestBody Banner banner) {
        Banner updated = bannerService.update(bannerId, banner);
        return createResponseEntity(HttpStatus.OK,
                "Successfully updated a banner with ID=" + bannerId, updated);
    }

    /**
     * Deletes a banner with the given ID from the database and returns a response-entity.
     *
     * @param bannerId id of the banner to be removed
     * @return a response-entity
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{bannerId}")
    public ResponseEntity<?> deleteBanner(@PathVariable Long bannerId) {
        bannerService.removeBanner(bannerId);
        return createResponseEntity(HttpStatus.OK,
                "Successfully removed the banner with ID=" + bannerId);
    }

    /**
     * Deletes all banners from the database.
     *
     * @return a response-entity
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    public ResponseEntity<?> deleteAllBanners() {
        bannerService.deleteBanners();
        return createResponseEntity(HttpStatus.OK,
                "Successfully deleted all banners");
    }

    @ExceptionHandler(BannerNotFoundException.class)
    public ResponseEntity<?> handleBannerNotFoundException(BannerNotFoundException ex) {
        return createResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }
}
