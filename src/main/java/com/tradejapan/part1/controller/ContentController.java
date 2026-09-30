package com.tradejapan.part1.controller;

import com.tradejapan.part1.model.Content;
import com.tradejapan.part1.model.User;
import com.tradejapan.part1.repository.ContentRepository;
import com.tradejapan.part1.repository.UserRepository;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Content> getAllContent() {
        return contentRepository.findAll();
    }

    @GetMapping("/{pageName}")
    public ResponseEntity<Content> getContent(@PathVariable String pageName) {
        Optional<Content> content = contentRepository.findByPageName(pageName);
        return content.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Content> saveContent(@RequestBody Content content) {
        String cleanHtml = Jsoup.clean(content.getHtmlContent(), Safelist.relaxed());
        content.setHtmlContent(cleanHtml);

        Long adminId = currentAdminId();
        LocalDateTime now = LocalDateTime.now();

        Optional<Content> existing = contentRepository.findByPageName(content.getPageName());
        if (existing.isPresent()) {
            Content toUpdate = existing.get();
            toUpdate.setTitle(content.getTitle());
            toUpdate.setHtmlContent(cleanHtml);
            toUpdate.setUpdatedBy(adminId);
            toUpdate.setUpdatedAt(now);
            return ResponseEntity.ok(contentRepository.save(toUpdate));
        }

        content.setUpdatedBy(adminId);
        content.setUpdatedAt(now);
        return ResponseEntity.ok(contentRepository.save(content));
    }

    private Long currentAdminId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> admin = userRepository.findByEmail(email);
        return admin.map(User::getId).orElse(null);
    }
}