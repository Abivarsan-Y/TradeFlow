package com.admin.cms.config;
import com.admin.cms.model.AuthProvider;
import com.admin.cms.model.Role;
import com.admin.cms.model.User;
import com.admin.cms.model.UserStatus;
import com.admin.cms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("superadmin@example.com").isEmpty()) {
            User sa = new User();
            sa.setEmail("superadmin@example.com");
            sa.setPassword(passwordEncoder.encode("superadmin123"));
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
