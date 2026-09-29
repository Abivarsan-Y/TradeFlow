package com.tradejapan.part3.repository;

import com.tradejapan.part3.model.Inquiry;
import com.tradejapan.part3.model.InquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Inquiry> findByStatusOrderByCreatedAtDesc(InquiryStatus status);
    List<Inquiry> findAllByOrderByCreatedAtDesc();
}
