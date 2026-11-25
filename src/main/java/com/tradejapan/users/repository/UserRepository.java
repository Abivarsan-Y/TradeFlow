package com.tradejapan.users.repository;
import com.tradejapan.users.model.User;
import com.tradejapan.users.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    long countByRole(Role role);
}
