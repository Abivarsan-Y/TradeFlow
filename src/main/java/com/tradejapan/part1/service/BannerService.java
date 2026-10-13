package com.tradejapan.cms.service;

import com.tradejapan.cms.dto.BannerRequest;
import com.tradejapan.cms.model.Banner;
import com.tradejapan.cms.model.BannerSize;
import com.tradejapan.cms.model.MediaType;
import com.tradejapan.cms.repository.BannerRepository;
import com.tradejapan.part3.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business logic for banners. The controller only handles HTTP,
 * this class decides the rules (limits, defaults, not-found).
 */
@Service
public class BannerService {

    private static final int MAX_BIG   = 5;
    private static final int MAX_SMALL = 3;

    private final BannerRepository bannerRepository;

    // Constructor injection (better than @Autowired on a field, and easy to mock in tests)
    public BannerService(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    // Returns banners grouped: { big: [...], small1: [...], small2: [...] }
    public Map<String, Object> getGroupedBanners() {
        Map<String, Object> result = new HashMap<>();
        result.put("big",    bannerRepository.findByActiveTrueAndSectionNumIsNullOrderBySortOrderAsc());
        result.put("small1", bannerRepository.findByActiveTrueAndSectionNumOrderBySortOrderAsc(1));
        result.put("small2", bannerRepository.findByActiveTrueAndSectionNumOrderBySortOrderAsc(2));
        return result;
    }

    public Banner addBanner(BannerRequest request) {
        Banner banner = new Banner();
        copyFields(request, banner);

        if (request.getSectionNum() == null) {
            // BIG hero banner
            if (bannerRepository.countByActiveTrueAndSectionNumIsNull() >= MAX_BIG) {
                throw new IllegalArgumentException("Maximum " + MAX_BIG + " BIG banners allowed.");
            }
            banner.setSize(BannerSize.BIG);
            banner.setMediaType(request.getMediaType() != null ? request.getMediaType() : MediaType.IMAGE);
        } else {
            // SMALL banner (section 1 = left, 2 = right)
            if (bannerRepository.countByActiveTrueAndSectionNum(request.getSectionNum()) >= MAX_SMALL) {
                throw new IllegalArgumentException("Maximum " + MAX_SMALL + " SMALL banners allowed per section.");
            }
            banner.setSize(BannerSize.SMALL);
            banner.setMediaType(MediaType.IMAGE);
        }
        banner.setActive(true);
        return bannerRepository.save(banner);
    }

    public Banner updateBanner(Long id, BannerRequest request) {
        Banner banner = findOrThrow(id);
        copyFields(request, banner);
        return bannerRepository.save(banner);
    }

    public void deleteBanner(Long id) {
        // Fixes the old bug: deleting a missing banner now gives a clear 404
        Banner banner = findOrThrow(id);
        bannerRepository.delete(banner);
    }

    private Banner findOrThrow(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found with id " + id));
    }

    // Copies the editable fields from the request onto the entity
    private void copyFields(BannerRequest request, Banner banner) {
        banner.setSize(request.getSize());
        banner.setMediaType(request.getMediaType());
        banner.setImageUrl(request.getImageUrl());
        banner.setLinkUrl(request.getLinkUrl());
        banner.setLinkType(request.getLinkType());
        banner.setButtonText(request.getButtonText());
        banner.setTitle(request.getTitle());
        banner.setSubtitle(request.getSubtitle());
        banner.setSectionNum(request.getSectionNum());
        banner.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        banner.setActive(request.isActive());
    }
}