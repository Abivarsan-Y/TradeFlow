package com.tradejapan.part3.repository;

import com.tradejapan.part3.model.Purchase;
import com.tradejapan.part3.model.PurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Purchase> findByStatusOrderByCreatedAtDesc(PurchaseStatus status);
    List<Purchase> findAllByOrderByCreatedAtDesc();
    boolean existsByInquiryId(Long inquiryId);
    Optional<Purchase> findByInquiryId(Long inquiryId);
}
