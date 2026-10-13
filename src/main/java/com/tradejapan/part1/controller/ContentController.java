package com.tradejapan.part1.controller;

import com.tradejapan.part1.model.Content;
import com.tradejapan.part1.service.ContentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    public List<Content> getAllContent() {
        return contentService.getAllContent();
    }

    @GetMapping("/{pageName}")
    public ResponseEntity<Content> getContent(@PathVariable String pageName) {
        return ResponseEntity.ok(contentService.getByPageName(pageName));
    }

    @PostMapping
    public ResponseEntity<Content> saveContent(@RequestBody Content content, Authentication authentication) {
        return ResponseEntity.ok(contentService.saveContent(content, authentication.getName()));
    }
}