package com.tradejapan.cms.controller;

import com.tradejapan.cms.dto.CreateAdminRequest;
import com.tradejapan.cms.dto.UpdateAdminRequest;
import com.tradejapan.cms.model.User;
import com.tradejapan.cms.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class SuperAdminController {

    private final AdminUserService adminUserService;

    public SuperAdminController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllAdmins() {
        return ResponseEntity.ok(adminUserService.getAllAdmins());
    }

    @PostMapping
    public ResponseEntity<?> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        try {
            return ResponseEntity.ok(adminUserService.createAdmin(request));
        } catch (IllegalArgumentException ex) {
            // Plain text, because the dashboard shows it directly
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable Long id,
                                         @Valid @RequestBody UpdateAdminRequest request) {
        adminUserService.updateAdmin(id, request);
        return ResponseEntity.ok("Admin updated successfully");
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> toggleAdminStatus(@PathVariable Long id, @RequestParam boolean active) {
        try {
            return ResponseEntity.ok(adminUserService.setStatus(id, active));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{id}/reset-password")
    public ResponseEntity<?> resetAdminPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            adminUserService.resetPassword(id, body.get("newPassword"));
            return ResponseEntity.ok("Password reset successful");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}