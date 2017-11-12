package ch.wisv.areafiftylan.web.banner.service;

import ch.wisv.areafiftylan.web.banner.model.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.Optional;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    // [Start, End)
    Optional<Banner> findByStartDateGreaterThanEqualAndEndDateLessThan(Date dateGreater, Date dateLess);

}
