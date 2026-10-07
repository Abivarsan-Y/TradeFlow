package com.tradejapan.part1.controller;

import com.tradejapan.part1.dto.UpdateAdminRequest;
import com.tradejapan.part1.model.AuthProvider;
import com.tradejapan.part1.model.Role;
import com.tradejapan.part1.model.User;
import com.tradejapan.part1.model.UserStatus;
import com.tradejapan.part1.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.tradejapan.part1.dto.CreateAdminRequest;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/users")
public class SuperAdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<List<User>> getAllAdmins() {
        return ResponseEntity.ok(userRepository.findByRole(Role.ROLE_ADMIN));
    }

    @PostMapping
    public ResponseEntity<?> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already taken");
        }
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setWhatsappNo(request.getWhatsappNo());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setAuthProvider(AuthProvider.LOCAL);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> toggleAdminStatus(@PathVariable Long id, @RequestParam boolean active) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getRole() == Role.ROLE_ADMIN) {
                user.setStatus(active ? UserStatus.ACTIVE : UserStatus.INACTIVE);
                return ResponseEntity.ok(userRepository.save(user));
            }
            return ResponseEntity.badRequest().body("Can only modify ADMIN role");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/reset-password")
    public ResponseEntity<?> resetAdminPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body("Password is required");
        }
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getRole() == Role.ROLE_ADMIN) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return ResponseEntity.ok("Password reset successful");
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable Long id,
                                         @Valid @RequestBody UpdateAdminRequest request) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        if (user.getRole() == Role.ROLE_SUPER_ADMIN) {
            return ResponseEntity.status(403).body("Cannot edit Super Admin");
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setWhatsappNo(request.getWhatsappNo());
        userRepository.save(user);
        return ResponseEntity.ok("Admin updated successfully");
    }
}