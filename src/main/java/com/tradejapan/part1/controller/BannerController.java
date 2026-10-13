package com.tradejapan.part1.controller;

import com.tradejapan.part1.dto.BannerRequest;
import com.tradejapan.part1.service.BannerService;
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

    @GetMapping
    public ResponseEntity<Map<String, Object>> getBanners() {
        return ResponseEntity.ok(bannerService.getGroupedBanners());
    }

    @PostMapping
    public ResponseEntity<?> addBanner(@RequestBody BannerRequest request) {
        try {
            return ResponseEntity.ok(bannerService.addBanner(request));
        } catch (IllegalArgumentException ex) {
            // plain text, because dashboard.html shows it directly in a toast
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBanner(@PathVariable Long id, @RequestBody BannerRequest request) {
        try {
            return ResponseEntity.ok(bannerService.updateBanner(id, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok("Deleted");
    }
}