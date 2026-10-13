package com.tradejapan.cms.controller;

import com.tradejapan.cms.dto.BannerRequest;
import com.tradejapan.cms.service.BannerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/banners")
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    // Returns banners grouped: { big: [...], small1: [...], small2: [...] }
    @GetMapping
    public ResponseEntity<Map<String, Object>> getBanners() {
        return ResponseEntity.ok(bannerService.getGroupedBanners());
    }

    @PostMapping
    public ResponseEntity<?> addBanner(@RequestBody BannerRequest request) {
        try {
            return ResponseEntity.ok(bannerService.addBanner(request));
        } catch (IllegalArgumentException ex) {
            // Plain text message, because dashboard.html shows it directly in a toast
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBanner(@PathVariable Long id, @RequestBody BannerRequest request) {
        // Missing banner -> ResourceNotFoundException -> 404 (GlobalExceptionHandler)
        return ResponseEntity.ok(bannerService.updateBanner(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok("Deleted");
    }
}