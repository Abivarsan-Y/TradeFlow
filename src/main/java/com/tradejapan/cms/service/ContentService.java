package com.tradejapan.cms.service;

import com.tradejapan.cms.model.Content;
import com.tradejapan.users.model.User;
import com.tradejapan.cms.repository.ContentRepository;
import com.tradejapan.users.repository.UserRepository;
import com.tradejapan.common.exception.ResourceNotFoundException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContentService {

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

    public ContentService(ContentRepository contentRepository, UserRepository userRepository) {
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
    }

    public List<Content> getAllContent() {
        return contentRepository.findAll();
    }

    public Content getByPageName(String pageName) {
        return contentRepository.findByPageName(pageName)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found: " + pageName));
    }

    // Creates the page if missing, otherwise updates it
    public Content saveContent(Content request, String adminEmail) {
        String html = request.getHtmlContent() != null ? request.getHtmlContent() : "";
        String cleanHtml = Jsoup.clean(html, Safelist.relaxed());

        Long adminId = userRepository.findByEmail(adminEmail).map(User::getId).orElse(null);

        Content content = contentRepository.findByPageName(request.getPageName())
                .orElseGet(() -> {
                    Content created = new Content();
                    created.setPageName(request.getPageName());
                    return created;
                });

        content.setTitle(request.getTitle());
        content.setHtmlContent(cleanHtml);
        content.setUpdatedBy(adminId);
        content.setUpdatedAt(LocalDateTime.now());
        return contentRepository.save(content);
    }
}