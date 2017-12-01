package ch.wisv.areafiftylan.web.banner.service;

import ch.wisv.areafiftylan.web.banner.model.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.Optional;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    // Note that the latest inserted is returned in case of multiple results.
    Optional<Banner> findFirstByStartDateBeforeAndEndDateAfterOrderByIdDesc(Date dateGreater, Date dateLess);

}
