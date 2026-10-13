package com.tradejapan.part3.repository;

import com.tradejapan.part3.model.Purchase;
import com.tradejapan.part3.model.PurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Purchase> findByStatusOrderByCreatedAtDesc(PurchaseStatus status);
    List<Purchase> findAllByOrderByCreatedAtDesc();
    long countByCreatedAtAfter(java.time.LocalDateTime time);
    long countByProcessedBy(Long processedBy);
    /** [userId, number of purchases] for every buyer who has at least one (used by Manage Customers). */
    @Query("select p.userId, count(p) from Purchase p group by p.userId")
    List<Object[]> countPerUser();
    List<Purchase> findTop15ByOrderByCreatedAtDesc();
    boolean existsByInquiryId(Long inquiryId);
    Optional<Purchase> findByInquiryId(Long inquiryId);
    List<Purchase> findByInquiryIdIn(java.util.Collection<Long> inquiryIds);
}
