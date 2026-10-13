package com.tradejapan.cms.service;

import com.tradejapan.cms.model.User;
import com.tradejapan.cms.repository.UserRepository;
import com.tradejapan.part3.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Logged-in user's own profile: view, edit, change password.
 * The email always comes from the JWT (via the controller), never from the request body.
 */
@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // The password hash is never sent: User.password is WRITE_ONLY in JSON
    public User getProfile(String email) {
        return findOrThrow(email);
    }

    // Only these three fields can be edited (role, status, email stay untouched)
    public User updateProfile(String email, User updated) {
        User user = findOrThrow(email);
        user.setFirstName(updated.getFirstName());
        user.setLastName(updated.getLastName());
        user.setWhatsappNo(updated.getWhatsappNo());
        return userRepository.save(user);
    }

    // Needs the current password first, so a stolen session can't lock the owner out
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = findOrThrow(email);

        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private User findOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}