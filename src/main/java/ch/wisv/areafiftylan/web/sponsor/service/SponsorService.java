package ch.wisv.areafiftylan.web.sponsor.service;

import ch.wisv.areafiftylan.web.sponsor.model.Sponsor;
import ch.wisv.areafiftylan.web.sponsor.model.SponsorType;

import java.util.Collection;

public interface SponsorService {

    Sponsor createSponsor(Sponsor sponsor);

    Collection<Sponsor> getAllSponsors();

    Collection<Sponsor> getAllSponsorsOfType(SponsorType type);

    void deleteSponsor(Long sponsorId);

    void deleteAllSponsors();
}
