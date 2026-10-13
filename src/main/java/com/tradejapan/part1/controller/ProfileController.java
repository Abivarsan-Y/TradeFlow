package com.tradejapan.part1.controller;

import com.tradejapan.part1.model.User;
import com.tradejapan.part1.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<User> getProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getProfile(authentication.getName()));
    }

    @PutMapping
    public ResponseEntity<User> updateProfile(@RequestBody User updatedUser, Authentication authentication) {
        return ResponseEntity.ok(profileService.updateProfile(authentication.getName(), updatedUser));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, Authentication authentication) {
        try {
            profileService.changePassword(
                    authentication.getName(),
                    body.get("currentPassword"),
                    body.get("newPassword"));
            return ResponseEntity.ok("Password updated successfully");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}