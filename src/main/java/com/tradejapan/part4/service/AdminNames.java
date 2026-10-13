package com.tradejapan.part4.service;

import com.tradejapan.part1.model.Role;
import com.tradejapan.part1.model.User;
import com.tradejapan.part1.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AdminNames {

    private final UserRepository userRepository;

    public AdminNames(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User currentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new AccessDeniedException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user not found: " + email
                        )
                );
    }

    public Long currentCustomerId() {

        User user = currentUser();

        if (user.getRole() != Role.ROLE_CUSTOMER) {
            throw new AccessDeniedException(
                    "Customer access required"
            );
        }

        return user.getId();
    }

    public Long currentAdminId() {

        User user = currentUser();

        if (user.getRole() != Role.ROLE_ADMIN
                && user.getRole() != Role.ROLE_SUPER_ADMIN) {

            throw new AccessDeniedException(
                    "Admin access required"
            );
        }

        return user.getId();
    }

    public String nameOf(Long userId) {

        if (userId == null) {
            return null;
        }

        return userRepository.findById(userId)
                .map(this::displayName)
                .orElse("Unknown User");
    }

    private String displayName(User user) {

        String firstName =
                user.getFirstName() == null
                        ? ""
                        : user.getFirstName().trim();

        String lastName =
                user.getLastName() == null
                        ? ""
                        : user.getLastName().trim();

        String fullName =
                (firstName + " " + lastName).trim();

        if (!fullName.isBlank()) {
            return fullName;
        }

        return user.getEmail();
    }
}