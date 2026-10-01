package com.tradejapan.cms.repository;
import com.tradejapan.cms.model.User;
import com.tradejapan.cms.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    long countByRole(Role role);
}
