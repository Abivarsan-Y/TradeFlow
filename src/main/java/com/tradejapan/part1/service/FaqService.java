package com.tradejapan.cms.service;

import com.tradejapan.cms.model.FaqItem;
import com.tradejapan.cms.repository.FaqItemRepository;
import com.tradejapan.part3.exception.ResourceNotFoundException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for FAQ items, including HTML sanitising (XSS protection).
 */
@Service
public class FaqService {

    private final FaqItemRepository faqRepository;

    public FaqService(FaqItemRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    // Public page: only active FAQs
    public List<FaqItem> getActiveFaqs() {
        return faqRepository.findByActiveTrueOrderByOrderAsc();
    }

    // Admin page: all FAQs, including hidden ones
    public List<FaqItem> getAllFaqs() {
        return faqRepository.findAllByOrderByOrderAsc();
    }

    public FaqItem addFaq(FaqItem request) {
        FaqItem faq = new FaqItem();   // new object, so a client-sent "id" is ignored
        faq.setQuestion(request.getQuestion());
        faq.setAnswer(sanitize(request.getAnswer()));
        faq.setOrder(request.getOrder());
        faq.setActive(request.isActive());
        return faqRepository.save(faq);
    }

    public FaqItem updateFaq(Long id, FaqItem updated) {
        FaqItem faq = findOrThrow(id);
        faq.setQuestion(updated.getQuestion());
        faq.setAnswer(sanitize(updated.getAnswer()));
        faq.setOrder(updated.getOrder());
        return faqRepository.save(faq);
    }

    public FaqItem toggleActive(Long id, boolean active) {
        FaqItem faq = findOrThrow(id);
        faq.setActive(active);
        return faqRepository.save(faq);
    }

    public void deleteFaq(Long id) {
        // Fixes the old bug: a missing FAQ now gives a clear 404
        faqRepository.delete(findOrThrow(id));
    }

    private FaqItem findOrThrow(Long id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ not found with id " + id));
    }

    // Keeps line breaks, removes dangerous HTML such as <script>
    private String sanitize(String answer) {
        String text = answer != null ? answer : "";
        return Jsoup.clean(text.replace("\n", "<br>"), Safelist.relaxed());
    }
}