package com.tradejapan.cms.repository;
import com.tradejapan.cms.model.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {
    Optional<Content> findByPageName(String pageName);
}