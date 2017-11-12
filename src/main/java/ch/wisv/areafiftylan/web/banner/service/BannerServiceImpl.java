package ch.wisv.areafiftylan.web.banner.service;

import ch.wisv.areafiftylan.exception.BannerNotFoundException;
import ch.wisv.areafiftylan.exception.ConflictingDateRangeException;
import ch.wisv.areafiftylan.web.banner.model.Banner;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;

@Service
public class BannerServiceImpl implements BannerService {

    private BannerRepository bannerRepository;

    public BannerServiceImpl(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    @Override
    public Collection<Banner> getAllBanners() {
        return bannerRepository.findAll();
    }

    @Override
    public Banner getCurrentbanner() {
        LocalDate now = LocalDate.now();
        return bannerRepository.findByStartDateGreaterThanEqualAndEndDateLessThan(now, now)
                .orElseThrow(BannerNotFoundException::new);
    }

    @Override
    public Banner addBanner(Banner banner) {
        if (hasConflictingDates(banner.getStartDate(), banner.getEndDate()))
            throw new ConflictingDateRangeException();

        return bannerRepository.save(banner);
    }

    @Override
    public Banner update(Long bannerId, Banner banner) {
        Banner current = bannerRepository.findOne(bannerId);

        if (hasConflictingDates(banner.getStartDate(), banner.getEndDate()))
            throw new ConflictingDateRangeException();

        return bannerRepository.saveAndFlush(current);
    }

    @Override
    public void removeBanner(Long id) {
        bannerRepository.delete(id);
    }

    @Override
    public void deleteBanners() {
        bannerRepository.deleteAll();
    }

    private boolean hasConflictingDates(LocalDate startDate, LocalDate endDate) {
        // ... WHERE new_end >= existing_start AND new_start < existing_end ;
        return bannerRepository.findByStartDateGreaterThanEqualAndEndDateLessThan(endDate, startDate).isPresent();
    }
}
