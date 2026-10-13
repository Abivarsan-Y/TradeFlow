package com.tradejapan.part1.service;

import com.tradejapan.part1.model.Role;
import com.tradejapan.part1.model.User;
import com.tradejapan.part1.repository.UserRepository;
import com.tradejapan.part3.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Password hash is never sent: User.password is WRITE_ONLY in JSON
    public User getProfile(String email) {
        return findOrThrow(email);
    }

    public User updateProfile(String email, User updated) {
        User user = findOrThrow(email);
        // An Admin cannot edit the profile here: the Super Admin changes admin details in Admin panel > Admins
        if (user.getRole() == Role.ROLE_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Admin profiles are managed by the Super Admin (Admin panel > Admins).");
        }
        user.setFirstName(updated.getFirstName());
        user.setLastName(updated.getLastName());
        user.setWhatsappNo(updated.getWhatsappNo());
        return userRepository.save(user);
    }

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