package ch.wisv.areafiftylan.web.banner.service;

import ch.wisv.areafiftylan.web.banner.model.Banner;

import java.util.Collection;

public interface BannerService {

    Collection<Banner> getAllBanners();

    Banner getCurrentbanner();

    Banner addBanner(Banner banner);

    Banner update(Long bannerId, Banner banner);

    void removeBanner(Long id);

    void deleteBanners();

}
