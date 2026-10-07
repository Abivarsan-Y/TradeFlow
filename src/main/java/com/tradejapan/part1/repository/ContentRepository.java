package com.tradejapan.part1.repository;
import com.tradejapan.part1.model.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {
    Optional<Content> findByPageName(String pageName);
}