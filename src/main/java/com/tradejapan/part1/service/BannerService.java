package com.tradejapan.part1.service;

import com.tradejapan.part1.dto.BannerRequest;
import com.tradejapan.part1.model.Banner;
import com.tradejapan.part1.model.BannerSize;
import com.tradejapan.part1.model.MediaType;
import com.tradejapan.part1.repository.BannerRepository;
import com.tradejapan.part3.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class BannerService {

    private static final int MAX_BIG   = 5;
    private static final int MAX_SMALL = 3;

    private final BannerRepository bannerRepository;

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
        validateSortOrder(request.getSectionNum(), request.getSortOrder());

        Banner banner = new Banner();
        copyFields(request, banner);

        if (request.getSectionNum() == null) {
            if (bannerRepository.countByActiveTrueAndSectionNumIsNull() >= MAX_BIG) {
                throw new IllegalArgumentException("Maximum " + MAX_BIG + " BIG banners allowed.");
            }
            banner.setSize(BannerSize.BIG);
            banner.setMediaType(request.getMediaType() != null ? request.getMediaType() : MediaType.IMAGE);
        } else {
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
        validateSortOrder(request.getSectionNum(), request.getSortOrder());
        Banner banner = findOrThrow(id);
        copyFields(request, banner);
        return bannerRepository.save(banner);
    }

    public void deleteBanner(Long id) {
        bannerRepository.delete(findOrThrow(id));
    }

    private Banner findOrThrow(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found with id " + id));
    }

    // Sort order is the position inside its group: BIG 1 to 5, SMALL 1 to 3
    private void validateSortOrder(Integer sectionNum, Integer sortOrder) {
        int max = (sectionNum == null) ? MAX_BIG : MAX_SMALL;
        if (sortOrder == null || sortOrder < 1 || sortOrder > max) {
            throw new IllegalArgumentException("Sort order for " + (sectionNum == null ? "BIG" : "SMALL")
                    + " banners must be between 1 and " + max + ".");
        }
    }

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
        banner.setSortOrder(request.getSortOrder());
        banner.setActive(request.isActive());
    }
}