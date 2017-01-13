package ch.wisv.areafiftylan.web.sponsor.controller;

import ch.wisv.areafiftylan.web.sponsor.model.Sponsor;
import ch.wisv.areafiftylan.web.sponsor.model.SponsorType;
import ch.wisv.areafiftylan.web.sponsor.service.SponsorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("/web/sponsors")
public class SponsorController {

    private SponsorService sponsorService;

    @Autowired
    public SponsorController(SponsorService sponsorService) {
        this.sponsorService = sponsorService;
    }

    /**
     * Creates a new sponsor.
     * @param sponsor The new sponsor.
     * @return The status of your request.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<?> createSponsor(@RequestBody @Validated Sponsor sponsor) {
        sponsor = sponsorService.createSponsor(sponsor);
        return createResponseEntity(HttpStatus.CREATED, "Sponsor successfully added.", sponsor);
    }

    /**
     * Gets all sponsors.
     * @return A collection with all sponsors.
     */
    @GetMapping
    public Collection<Sponsor> getAllSponsors() {
        return sponsorService.getAllSponsors();
    }

    /**
     * Gets all sponsors of a type.
     * @param type Type of the sponsor.
     * @return A collection with all sponsors of that type.
     */
    @GetMapping("/{type}")
    public Collection<Sponsor> getAllSponsorsOfType(@PathVariable SponsorType type) {
        return sponsorService.getAllSponsorsOfType(type);
    }

    /**
     * Updates the sponsor at the ID with the values in the body.
     * @param sponsorId the ID of the sponsor.
     * @param sponsor The new values.
     * @return The status of your request.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{sponsorId]")
    public ResponseEntity<?> updateSponsor(@PathVariable Long sponsorId, @RequestBody @Validated Sponsor sponsor) {
        sponsor = sponsorService.updateSponsor(sponsorId, sponsor);
        return createResponseEntity(HttpStatus.ACCEPTED, "Sponsor successfully updated.", sponsor);
    }

    /**
     * Deletes a single sponsor.
     * @param sponsorId the ID of the sponsor to be deleted.
     * @return The status of your request.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/sponsorId")
    public ResponseEntity<?> deleteSponsor(@PathVariable Long sponsorId) {
        sponsorService.deleteSponsor(sponsorId);
        return createResponseEntity(HttpStatus.NO_CONTENT, "Sponsor successfully deleted.");
    }

    /**
     * Deletes all sponsors. This is useful after a LAN, when no longer having any sponsors.
     * @return The status of your request.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    public ResponseEntity<?> deleteAllSponsors() {
        sponsorService.deleteAllSponsors();
        return createResponseEntity(HttpStatus.NO_CONTENT, "Successfully deleted all sponsors.");
    }
}
