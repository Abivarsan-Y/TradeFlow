package com.admin.cms.security;

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
                .requestMatchers(HttpMethod.GET, "/api/content/**", "/api/faqs/**", "/api/banners/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/content/**", "/api/faqs/**", "/api/banners/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/content/**", "/api/faqs/**", "/api/banners/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/content/**", "/api/faqs/**", "/api/banners/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/**/*.html")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/**/*.css")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/**/*.js")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/")).permitAll()
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
