package com.tradejapan.cms.controller;

import com.tradejapan.cms.model.Banner;
import com.tradejapan.cms.model.BannerSize;
import com.tradejapan.cms.model.MediaType;
import com.tradejapan.cms.repository.BannerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banners")
public class BannerController {

    private static final int MAX_BIG   = 5;
    private static final int MAX_SMALL = 3;

    @Autowired
    private BannerRepository bannerRepository;

    // Returns banners grouped: { big: [...], small1: [...], small2: [...] }
    @GetMapping
    public ResponseEntity<Map<String, Object>> getBanners() {
        List<Banner> big    = bannerRepository.findByActiveTrueAndSectionNumIsNullOrderBySortOrderAsc();
        List<Banner> small1 = bannerRepository.findByActiveTrueAndSectionNumOrderBySortOrderAsc(1);
        List<Banner> small2 = bannerRepository.findByActiveTrueAndSectionNumOrderBySortOrderAsc(2);

        Map<String, Object> result = new HashMap<>();
        result.put("big",    big);
        result.put("small1", small1);
        result.put("small2", small2);
        return ResponseEntity.ok(result);
    }

    // The sort order is the position inside its group: BIG banners 1 to 5, SMALL banners 1 to 3
    private String sortOrderError(Integer sectionNum, Integer sortOrder) {
        int max = (sectionNum == null) ? MAX_BIG : MAX_SMALL;
        if (sortOrder == null || sortOrder < 1 || sortOrder > max) {
            return "Sort order for " + (sectionNum == null ? "BIG" : "SMALL") + " banners must be between 1 and " + max + ".";
        }
        return null;
    }

    @PostMapping
    public ResponseEntity<?> addBanner(@RequestBody Banner banner) {
        String orderError = sortOrderError(banner.getSectionNum(), banner.getSortOrder());
        if (orderError != null) {
            return ResponseEntity.badRequest().body(orderError);
        }
        // Enforce limits
        if (banner.getSectionNum() == null) {
            long count = bannerRepository.countByActiveTrueAndSectionNumIsNull();
            if (count >= MAX_BIG) {
                return ResponseEntity.badRequest().body("Maximum " + MAX_BIG + " BIG banners allowed.");
            }
            banner.setSize(BannerSize.BIG);
            banner.setMediaType(banner.getMediaType() != null ? banner.getMediaType() : MediaType.IMAGE);
        } else {
            long count = bannerRepository.countByActiveTrueAndSectionNum(banner.getSectionNum());
            if (count >= MAX_SMALL) {
                return ResponseEntity.badRequest().body("Maximum " + MAX_SMALL + " SMALL banners allowed per section.");
            }
            banner.setSize(BannerSize.SMALL);
            banner.setMediaType(MediaType.IMAGE);
        }
        banner.setActive(true);
        return ResponseEntity.ok(bannerRepository.save(banner));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBanner(@PathVariable Long id, @RequestBody Banner updated) {
        String orderError = sortOrderError(updated.getSectionNum(), updated.getSortOrder());
        if (orderError != null) {
            return ResponseEntity.badRequest().body(orderError);
        }
        return bannerRepository.findById(id).map(banner -> {
            banner.setSize(updated.getSize());
            banner.setMediaType(updated.getMediaType());
            banner.setImageUrl(updated.getImageUrl());
            banner.setLinkUrl(updated.getLinkUrl());
            banner.setLinkType(updated.getLinkType());
            banner.setButtonText(updated.getButtonText());
            banner.setTitle(updated.getTitle());
            banner.setSubtitle(updated.getSubtitle());
            banner.setSectionNum(updated.getSectionNum());
            banner.setSortOrder(updated.getSortOrder());
            banner.setActive(updated.isActive());
            return ResponseEntity.ok(bannerRepository.save(banner));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBanner(@PathVariable Long id) {
        bannerRepository.deleteById(id);
        return ResponseEntity.ok("Deleted");
    }
}