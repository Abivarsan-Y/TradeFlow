package com.tradejapan.cms.service;

import com.tradejapan.cms.dto.AuthResponse;
import com.tradejapan.cms.dto.LoginRequest;
import com.tradejapan.cms.dto.RegisterRequest;
import com.tradejapan.cms.model.AuthProvider;
import com.tradejapan.cms.model.Role;
import com.tradejapan.cms.model.User;
import com.tradejapan.cms.model.UserStatus;
import com.tradejapan.cms.repository.UserRepository;
import com.tradejapan.cms.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Error: Email is already taken!");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setWhatsappNo(request.getWhatsappNo());
        user.setRole(Role.ROLE_CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setAcceptedTermsAt(LocalDateTime.now());

        userRepository.save(user);
    }

    // Customer login: staff accounts must use the admin login page
    public AuthResponse login(LoginRequest request) {
        AuthResponse response = authenticateAndBuildResponse(request);
        if (isStaff(response.getRole())) {
            // checked after the password, so a wrong password never reveals that the account is staff
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This is a staff account. Please use the admin login page.");
        }
        return response;
    }

    // Staff login: only ADMIN or SUPER_ADMIN may continue
    public AuthResponse adminLogin(LoginRequest request) {
        AuthResponse response = authenticateAndBuildResponse(request);
        if (!isStaff(response.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        return response;
    }

    private boolean isStaff(String role) {
        return role.equals(Role.ROLE_ADMIN.name()) || role.equals(Role.ROLE_SUPER_ADMIN.name());
    }

    // Wrong password -> BadCredentialsException -> 401 (GlobalExceptionHandler)
    private AuthResponse authenticateAndBuildResponse(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String role = user.getRole().name();
        String jwt = tokenProvider.generateToken(user.getEmail(), role);
        return new AuthResponse(jwt, role);
    }
}