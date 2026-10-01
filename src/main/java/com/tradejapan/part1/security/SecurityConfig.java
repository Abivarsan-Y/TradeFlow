package com.tradejapan.cms.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                // --- Inventory module (public browsing) ---
                .requestMatchers(HttpMethod.GET, "/api/vehicles/**", "/api/features").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/content/**", "/api/faqs/**", "/api/banners/**").permitAll()
                // --- Contact Us + testimonials (Function 4) ---
                .requestMatchers(HttpMethod.POST, "/api/contact-messages").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/testimonials/approved").permitAll()
                // --- public site settings (WhatsApp number) ---
                .requestMatchers(HttpMethod.GET, "/api/settings/public").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/content/**", "/api/faqs/**", "/api/banners/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/content/**", "/api/faqs/**", "/api/banners/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/content/**", "/api/faqs/**", "/api/banners/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                // --- customer-only features: Admin and Super Admin accounts are not customers ---
                // (staff still use the shared read endpoints: GET /api/inquiries/{id}, /{id}/replies, /api/purchases/{id}, /{id}/invoice)
                .requestMatchers(HttpMethod.POST, "/api/inquiries", "/api/inquiries/*/replies", "/api/testimonials").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.GET, "/api/inquiries/me", "/api/purchases/me", "/api/testimonials/me").hasRole("CUSTOMER")
                .requestMatchers("/api/wishlist/**").hasRole("CUSTOMER")
                // --- Super admin only (user management + reports) ---
                .requestMatchers("/api/admin/users/**", "/api/admin/reports/**").hasRole("SUPER_ADMIN")
                // --- Inventory admin (vehicles) ---
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/**/*.html")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/**/*.css")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/**/*.js")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/images/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/")).permitAll()
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
