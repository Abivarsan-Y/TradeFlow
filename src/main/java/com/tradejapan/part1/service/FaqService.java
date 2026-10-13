package com.tradejapan.part1.service;

import com.tradejapan.part1.model.FaqItem;
import com.tradejapan.part1.repository.FaqItemRepository;
import com.tradejapan.part3.exception.ResourceNotFoundException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FaqService {

    private final FaqItemRepository faqRepository;

    public FaqService(FaqItemRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    public List<FaqItem> getActiveFaqs() {
        return faqRepository.findByActiveTrueOrderByOrderAsc();
    }

    public List<FaqItem> getAllFaqs() {
        return faqRepository.findAllByOrderByOrderAsc();
    }

    public FaqItem addFaq(FaqItem request) {
        FaqItem faq = new FaqItem();
        faq.setQuestion(request.getQuestion());
        faq.setAnswer(sanitize(request.getAnswer()));
        faq.setActive(request.isActive());
        // a new FAQ goes to the end of the list; the admin changes the order by dragging the rows
        int last = faqRepository.findAllByOrderByOrderAsc().stream()
                .map(FaqItem::getOrder).filter(o -> o != null).max(Integer::compare).orElse(0);
        faq.setOrder(last + 1);
        return faqRepository.save(faq);
    }

    // Drag and drop: ids are the FAQ ids in the new order
    public void reorderFaqs(List<Long> ids) {
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
        for (FaqItem f : remaining.values()) {   // any FAQ not in the list goes to the end
            f.setOrder(position++);
            changed.add(f);
        }
        faqRepository.saveAll(changed);
    }

    public FaqItem updateFaq(Long id, FaqItem updated) {
        FaqItem faq = findOrThrow(id);
        faq.setQuestion(updated.getQuestion());
        faq.setAnswer(sanitize(updated.getAnswer()));
        if (updated.getOrder() != null) faq.setOrder(updated.getOrder());
        return faqRepository.save(faq);
    }

    public FaqItem toggleActive(Long id, boolean active) {
        FaqItem faq = findOrThrow(id);
        faq.setActive(active);
        return faqRepository.save(faq);
    }

    public void deleteFaq(Long id) {
        faqRepository.delete(findOrThrow(id));
    }

    private FaqItem findOrThrow(Long id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ not found with id " + id));
    }

    private String sanitize(String answer) {
        String text = answer != null ? answer : "";
        return Jsoup.clean(text.replace("\n", "<br>"), Safelist.relaxed());
    }
}