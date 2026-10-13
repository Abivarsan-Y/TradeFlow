package com.tradejapan.part1.controller;

import com.tradejapan.part1.model.Role;
import com.tradejapan.part1.model.User;
import com.tradejapan.part1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<?> getProfile() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            User u = user.get();
            u.setPassword(null); // Don't send password hash
            return ResponseEntity.ok(u);
        }

        return ResponseEntity.notFound().build();
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody User updatedUser) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // An Admin cannot edit the profile here:
            // the Super Admin changes admin details in Admin panel > Admins
            if (user.getRole() == Role.ROLE_ADMIN) {
                return ResponseEntity
                        .status(403)
                        .body("Admin profiles are managed by the Super Admin (Admin panel > Admins).");
            }

            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setWhatsappNo(updatedUser.getWhatsappNo());

            userRepository.save(user);

            user.setPassword(null);

            return ResponseEntity.ok(user);
        }

        return ResponseEntity.notFound().build();
    }

    // Separate from profile edit: requires the current password before
    // allowing a change, so a hijacked session can't silently lock the
    // real owner out.
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        User user = userOpt.get();

        if (currentPassword == null ||
                !passwordEncoder.matches(currentPassword, user.getPassword())) {

            return ResponseEntity
                    .badRequest()
                    .body("Current password is incorrect");
        }

        if (newPassword == null || newPassword.length() < 8) {
            return ResponseEntity
                    .badRequest()
                    .body("New password must be at least 8 characters");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok("Password updated successfully");
    }
}