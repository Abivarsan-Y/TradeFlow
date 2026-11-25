package com.tradejapan.auth.controller;

import com.tradejapan.auth.dto.AuthResponse;
import com.tradejapan.auth.dto.LoginRequest;
import com.tradejapan.auth.dto.RegisterRequest;
import com.tradejapan.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            authService.register(registerRequest);
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponse> authenticateAdmin(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.adminLogin(loginRequest));
    }
}