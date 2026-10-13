package com.tradejapan.part3.service;

import com.tradejapan.part1.model.Role;
import com.tradejapan.part1.model.User;
import com.tradejapan.part1.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Resolves the logged-in user (from the JWT) so no endpoint trusts a userId sent by the browser. */
@Component
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in"));
    }

    public boolean isAdmin(User user) {
        return user.getRole() == Role.ROLE_ADMIN || user.getRole() == Role.ROLE_SUPER_ADMIN;
    }
}
