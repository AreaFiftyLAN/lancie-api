package ch.wisv.areafiftylan.web.banner.service;

import ch.wisv.areafiftylan.exception.BannerNotFoundException;
import ch.wisv.areafiftylan.web.banner.model.Banner;
import org.springframework.stereotype.Service;

import java.sql.Date;
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
        Date now = Date.valueOf(LocalDate.now());
        return bannerRepository.findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByIdDesc(now, now)
                .orElseThrow(BannerNotFoundException::new);
    }

    @Override
    public Banner addBanner(Banner banner) {
        return bannerRepository.save(banner);
    }

    @Override
    public Banner update(Long bannerId, Banner banner) {
        if (!bannerRepository.existsById(bannerId)) {
            throw new BannerNotFoundException();
        }

        return bannerRepository.saveAndFlush(banner);
    }

    @Override
    public void removeBanner(Long id) {
        bannerRepository.deleteById(id);
    }

    @Override
    public void deleteBanners() {
        bannerRepository.deleteAll();
    }
}
