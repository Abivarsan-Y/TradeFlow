package com.admin.cms.controller;

import com.admin.cms.dto.*;
import com.admin.cms.model.AuthProvider;
import com.admin.cms.model.Role;
import com.admin.cms.model.User;
import com.admin.cms.model.UserStatus;
import com.admin.cms.repository.UserRepository;
import com.admin.cms.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
        if (userOptional.isPresent()) {
            String role = userOptional.get().getRole().name();
            String jwt = tokenProvider.generateToken(loginRequest.getEmail(), role);
            return ResponseEntity.ok(new AuthResponse(jwt, role));
        }
        return ResponseEntity.badRequest().body("User not found");
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Email is already taken!");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setWhatsappNo(registerRequest.getWhatsappNo());
        user.setRole(Role.ROLE_CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setAcceptedTermsAt(LocalDateTime.now());
        // createdAt defaults to now() on the entity itself

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> authenticateAdmin(@Valid @RequestBody LoginRequest loginRequest) {
        // Admin login logic is same, but we can verify role here
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
        if (userOptional.isPresent() &&
            (userOptional.get().getRole() == Role.ROLE_ADMIN || userOptional.get().getRole() == Role.ROLE_SUPER_ADMIN)) {
            String role = userOptional.get().getRole().name();
            String jwt = tokenProvider.generateToken(loginRequest.getEmail(), role);
            return ResponseEntity.ok(new AuthResponse(jwt, role));
        }
        return ResponseEntity.status(403).body("Access Denied");
    }
}
