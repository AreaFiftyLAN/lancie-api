package ch.wisv.areafiftylan.web.sponsor.controller;

import ch.wisv.areafiftylan.exception.SponsorConstraintViolationException;
import ch.wisv.areafiftylan.web.sponsor.model.Sponsor;
import ch.wisv.areafiftylan.web.sponsor.model.SponsorType;
import ch.wisv.areafiftylan.web.sponsor.service.SponsorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static ch.wisv.areafiftylan.utils.ResponseEntityBuilder.createResponseEntity;

@RestController
@RequestMapping("/web/sponsor")
public class SponsorController {

    private SponsorService sponsorService;

    public SponsorController(SponsorService sponsorService) {
        this.sponsorService = sponsorService;
    }

    /**
     * Creates a new sponsor.
     * @param sponsor The new sponsor.
     * @return The status of your request.
     */
    @PreAuthorize("hasRole('COMMITTEE')")
    @PostMapping
    public ResponseEntity<?> createSponsor(@RequestBody @Validated Sponsor sponsor) {
        sponsor = sponsorService.createSponsor(sponsor);
        return createResponseEntity(HttpStatus.CREATED, "Sponsor successfully added.", sponsor);
    }

    /**
     * Gets all sponsors.
     * @return A collection with all sponsors.
     */
    @GetMapping
    public ResponseEntity<?> getAllSponsors() {
        Collection<Sponsor> sponsors = sponsorService.getAllSponsors();
        return createResponseEntity(HttpStatus.OK, "Sponsors retrieved.", sponsors);
    }

    /**
     * Gets all sponsors of a type.
     * @param type Type of the sponsor.
     * @return A collection with all sponsors of that type.
     */
    @GetMapping("/{type}")
    public ResponseEntity<?> getAllSponsorsOfType(@PathVariable SponsorType type) {
        Collection<Sponsor> sponsorsOfType = sponsorService.getAllSponsorsOfType(type);
        return createResponseEntity(HttpStatus.OK, "Sponsors of type " + type + " retrieved.", sponsorsOfType);
    }

    /**
     * Deletes a single sponsor.
     * @param sponsorId the ID of the sponsor to be deleted.
     * @return The status of your request.
     */
    @PreAuthorize("hasRole('COMMITTEE')")
    @DeleteMapping("/{sponsorId}")
    public ResponseEntity<?> deleteSponsor(@PathVariable Long sponsorId) {
        sponsorService.deleteSponsor(sponsorId);
        return createResponseEntity(HttpStatus.OK, "Sponsor successfully deleted.");
    }

    /**
     * Deletes all sponsors. This is useful after a LAN, when no longer having any sponsors.
     * @return The status of your request.
     */
    @PreAuthorize("hasRole('COMMITTEE')")
    @DeleteMapping
    public ResponseEntity<?> deleteAllSponsors() {
        sponsorService.deleteAllSponsors();
        return createResponseEntity(HttpStatus.OK, "Successfully deleted all sponsors.");
    }

    @ExceptionHandler(SponsorConstraintViolationException.class)
    public ResponseEntity<?> handleSponsorStillUsedInTournamentException(SponsorConstraintViolationException ex) {
        return createResponseEntity(HttpStatus.CONFLICT, ex.getMessage());
    }
}
