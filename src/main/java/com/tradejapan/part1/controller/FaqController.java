package com.tradejapan.part1.controller;

import com.tradejapan.part1.model.FaqItem;
import com.tradejapan.part1.repository.FaqItemRepository;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/faqs")
public class FaqController {

    @Autowired
    private FaqItemRepository faqRepository;

    @GetMapping
    public List<FaqItem> getFaqs() {
        return faqRepository.findByActiveTrueOrderByOrderAsc();
    }

    @GetMapping("/all")
    public List<FaqItem> getAllFaqs() {
        return faqRepository.findAllByOrderByOrderAsc();
    }

    @PostMapping
    public ResponseEntity<?> addFaq(@RequestBody FaqItem faq) {
        faq.setAnswer(Jsoup.clean((faq.getAnswer() != null ? faq.getAnswer() : "").replace("\n", "<br>"), Safelist.relaxed()));
        // a new FAQ goes to the end of the list; the admin changes the order by dragging the rows
        int last = faqRepository.findAllByOrderByOrderAsc().stream()
                .map(FaqItem::getOrder).filter(o -> o != null).max(Integer::compare).orElse(0);
        faq.setOrder(last + 1);
        return ResponseEntity.ok(faqRepository.save(faq));
    }

    // Drag and drop in the admin panel: the body is the list of FAQ ids in the new order
    @PutMapping("/reorder")
    public ResponseEntity<?> reorderFaqs(@RequestBody List<Long> ids) {
        Map<Long, FaqItem> remaining = new LinkedHashMap<>();
        for (FaqItem f : faqRepository.findAllByOrderByOrderAsc()) remaining.put(f.getId(), f);
        List<FaqItem> changed = new ArrayList<>();
        int position = 1;
        for (Long id : ids) {
            FaqItem f = remaining.remove(id);
            if (f != null) {
                f.setOrder(position++);
                changed.add(f);
            }
        }
        for (FaqItem f : remaining.values()) {      // any FAQ that was not in the list goes to the end
            f.setOrder(position++);
            changed.add(f);
        }
        faqRepository.saveAll(changed);
        return ResponseEntity.ok("Order saved");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFaq(@PathVariable Long id, @RequestBody FaqItem updated) {
        Optional<FaqItem> faqOpt = faqRepository.findById(id);
        if (faqOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        FaqItem faq = faqOpt.get();
        faq.setQuestion(updated.getQuestion());
        faq.setAnswer(Jsoup.clean((updated.getAnswer() != null ? updated.getAnswer() : "").replace("\n", "<br>"), Safelist.relaxed()));
        if (updated.getOrder() != null) faq.setOrder(updated.getOrder());   // the order is changed by dragging, not here
        return ResponseEntity.ok(faqRepository.save(faq));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> toggleActive(@PathVariable Long id, @RequestParam boolean active) {
        Optional<FaqItem> faqOpt = faqRepository.findById(id);
        if (faqOpt.isPresent()) {
            FaqItem faq = faqOpt.get();
            faq.setActive(active);
            return ResponseEntity.ok(faqRepository.save(faq));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFaq(@PathVariable Long id) {
        faqRepository.deleteById(id);
        return ResponseEntity.ok("Deleted");
    }
}