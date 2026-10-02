package com.tradejapan.cms.controller;

import com.tradejapan.cms.model.PasswordResetToken;
import com.tradejapan.cms.model.Role;
import com.tradejapan.cms.model.User;
import com.tradejapan.cms.repository.PasswordResetTokenRepository;
import org.springframework.transaction.annotation.Transactional;
import com.tradejapan.cms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    @PostMapping("/request")
    public ResponseEntity<?> requestReset(@RequestParam String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getRole() != Role.ROLE_CUSTOMER) {
                return ResponseEntity.badRequest().body("Admins cannot reset via email");
            }

            // Remove any previous token so only the newest link is valid
            tokenRepository.deleteByUser(user);
            tokenRepository.flush();

            PasswordResetToken token = new PasswordResetToken();
            token.setToken(UUID.randomUUID().toString());
            token.setUser(user);
            token.setExpiryDate(LocalDateTime.now().plusHours(1));
            // usedAt stays null -- this token is unused until confirmReset marks it
            tokenRepository.save(token);

            // In real app, send email with the token here
            return ResponseEntity.ok("Reset token created: " + token.getToken());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmReset(@RequestParam String token, @RequestParam String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isPresent()) {
            PasswordResetToken resetToken = tokenOpt.get();

            if (resetToken.getUsedAt() != null) {
                return ResponseEntity.badRequest().body("This reset link has already been used");
            }
            if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body("Token expired");
            }

            User user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Mark used instead of deleting, so there's an audit trail
            // of when this reset actually happened
            resetToken.setUsedAt(LocalDateTime.now());
            tokenRepository.save(resetToken);

            return ResponseEntity.ok("Password successfully reset");
        }
        return ResponseEntity.badRequest().body("Invalid token");
    }
}
