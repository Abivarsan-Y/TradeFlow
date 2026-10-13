package com.tradejapan.cms.service;

import com.tradejapan.cms.dto.CreateAdminRequest;
import com.tradejapan.cms.dto.UpdateAdminRequest;
import com.tradejapan.cms.model.AuthProvider;
import com.tradejapan.cms.model.Role;
import com.tradejapan.cms.model.User;
import com.tradejapan.cms.model.UserStatus;
import com.tradejapan.cms.repository.UserRepository;
import com.tradejapan.part3.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Super Admin rules for managing Admin accounts.
 */
@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllAdmins() {
        return userRepository.findByRole(Role.ROLE_ADMIN);
    }

    public User createAdmin(CreateAdminRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already taken");
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
        return userRepository.save(user);
    }

    public User updateAdmin(Long id, UpdateAdminRequest request) {
        User user = findOrThrow(id);
        if (user.getRole() == Role.ROLE_SUPER_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot edit Super Admin");
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setWhatsappNo(request.getWhatsappNo());
        return userRepository.save(user);
    }

    public User setStatus(Long id, boolean active) {
        User user = findOrThrow(id);
        if (user.getRole() != Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("Can only modify ADMIN role");
        }
        user.setStatus(active ? UserStatus.ACTIVE : UserStatus.INACTIVE);
        return userRepository.save(user);
    }

    // Staff have no self-service reset, so the Super Admin sets a new password
    public void resetPassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        User user = findOrThrow(id);
        if (user.getRole() != Role.ROLE_ADMIN) {
            throw new ResourceNotFoundException("Admin not found with id " + id);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id " + id));
    }
}