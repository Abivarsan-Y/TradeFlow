package com.tradejapan.cms.repository;
import com.tradejapan.cms.model.PasswordResetToken;
import com.tradejapan.cms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}
