package com.tradejapan.cms.repository;
import com.tradejapan.cms.model.FaqItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FaqItemRepository extends JpaRepository<FaqItem, Long> {
    // Public-facing FAQ list: only active ones, in display order
    List<FaqItem> findByActiveTrueOrderByOrderAsc();

    // Admin view: everything, including hidden/inactive ones
    List<FaqItem> findAllByOrderByOrderAsc();
}