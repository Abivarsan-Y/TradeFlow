package com.tradejapan.cms.config;

import com.tradejapan.cms.model.AuthProvider;
import com.tradejapan.cms.model.Role;
import com.tradejapan.cms.model.User;
import com.tradejapan.cms.model.UserStatus;
import com.tradejapan.cms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Runs once at startup and creates the first Super Admin account if it does not exist yet.
 * (The page texts and FAQs are NOT created here: they are written in data.sql.)
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // set in application.properties; put your own value in local.properties
    @Value("${app.superadmin.password:superadmin123}")
    private String superAdminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("superadmin@example.com").isEmpty()) {
            User sa = new User();
            sa.setEmail("superadmin@example.com");
            sa.setPassword(passwordEncoder.encode(superAdminPassword));
            sa.setFirstName("Super");
            sa.setLastName("Admin");
            sa.setWhatsappNo("+81-80-9876-5432");
            sa.setRole(Role.ROLE_SUPER_ADMIN);
            sa.setStatus(UserStatus.ACTIVE);
            sa.setAuthProvider(AuthProvider.LOCAL);
            userRepository.save(sa);
        }
    }
}
