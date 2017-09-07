package ch.wisv.areafiftylan.web.sponsor.service;

import ch.wisv.areafiftylan.web.sponsor.model.Sponsor;
import ch.wisv.areafiftylan.web.sponsor.model.SponsorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface SponsorRepository extends JpaRepository<Sponsor, Long> {

    Collection<Sponsor> findByType(SponsorType type);
}
