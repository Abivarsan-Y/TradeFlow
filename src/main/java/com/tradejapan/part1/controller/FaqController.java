package com.tradejapan.cms.controller;

import com.tradejapan.cms.model.FaqItem;
import com.tradejapan.cms.service.FaqService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
public class FaqController {

    private final FaqService faqService;

    public FaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    @GetMapping
    public List<FaqItem> getFaqs() {
        return faqService.getActiveFaqs();
    }

    @GetMapping("/all")
    public List<FaqItem> getAllFaqs() {
        return faqService.getAllFaqs();
    }

    @PostMapping
    public ResponseEntity<?> addFaq(@RequestBody FaqItem faq) {
        return ResponseEntity.ok(faqService.addFaq(faq));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFaq(@PathVariable Long id, @RequestBody FaqItem updated) {
        return ResponseEntity.ok(faqService.updateFaq(id, updated));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> toggleActive(@PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(faqService.toggleActive(id, active));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFaq(@PathVariable Long id) {
        faqService.deleteFaq(id);
        return ResponseEntity.ok("Deleted");
    }
}